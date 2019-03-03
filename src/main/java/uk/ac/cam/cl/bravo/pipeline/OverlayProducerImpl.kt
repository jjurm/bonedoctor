package uk.ac.cam.cl.bravo.pipeline

import com.jhlabs.image.FlipFilter
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import javafx.util.Pair
import uk.ac.cam.cl.bravo.dataset.BodypartView
import uk.ac.cam.cl.bravo.dataset.ImageSample
import uk.ac.cam.cl.bravo.overlay.ImageOverlay
import java.awt.image.BufferedImage

class OverlayProducerImpl(
    private val imageOverlay: ImageOverlay,
    private val base: BufferedImage,
    private val bodypartView: BodypartView,
    private val sample: ImageSample,
    private val downsample: Double,
    private val precision: Double
) : OverlayProducer {

    private val flipFilter = FlipFilter(FlipFilter.FLIP_H)

    private val requests = BehaviorSubject.create<Unit>()
    private val startComputation = requests.take(1).map { sample.loadPreprocessedImage() }

    private val overlayOriginal: Observable<Rated<kotlin.Pair<BufferedImage, BufferedImage>>> = startComputation
        .observeOn(Schedulers.computation())
        .map { loaded ->
            imageOverlay.fitImage(base, bodypartView, loaded, downsample, precision)
        }
        .withCache()

    private val overlayMirrored: Observable<Rated<kotlin.Pair<BufferedImage, BufferedImage>>> = startComputation
        .observeOn(Schedulers.computation())
        .map { loaded ->
            val mirrored = flipFilter.filter(loaded, null)
            imageOverlay.fitImage(base, bodypartView, mirrored, downsample, precision)
        }
        .withCache()

    private val overlayResult =
        Observable.combineLatest(
            overlayOriginal,
            overlayMirrored,
            BiFunction<Rated<kotlin.Pair<BufferedImage, BufferedImage>>, Rated<kotlin.Pair<BufferedImage, BufferedImage>>, Rated<Pair<BufferedImage, BufferedImage>>>
            { original, mirrored ->
                val best = listOf(original, mirrored).min()!!
                val (transformed, overlaid) = best.value
                Rated(Pair(transformed, overlaid), best.score)
            }
        )
            .withCache()

    override fun requestOverlay(): Observable<Rated<Pair<BufferedImage, BufferedImage>>> {
        requests.onNext(Unit)
        return overlayResult
    }

}
