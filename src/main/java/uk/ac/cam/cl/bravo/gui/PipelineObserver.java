package uk.ac.cam.cl.bravo.gui;

import javafx.css.Match;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.ac.cam.cl.bravo.dataset.BoneCondition;
import uk.ac.cam.cl.bravo.dataset.ImageSample;
import uk.ac.cam.cl.bravo.pipeline.MainPipeline;
import uk.ac.cam.cl.bravo.pipeline.Rated;
import uk.ac.cam.cl.bravo.pipeline.Uncertain;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class PipelineObserver  {

    private Main main;
    private InformationPanelController informationPanelController;
    private UploadController uploadController;
    private List<ImageExplorerController> imageExplorerControllers;
    private MatchListController matchListController;
    private AnalysisController analysisController;
    private MainController mainController;

    private MainPipeline mainPipeline = new MainPipeline();

    // TODO: Rename to GUIMainInterface
    public PipelineObserver() {

        /*
        * We start from the main GUI function. Then we pass a the main pipeline, created in the main function.
        * We then create the user interface in here by launching the first main function.
        * This main function will not yet have any guitomain interface to pass to its children
        * as the interface is not made.
        *
        * We need to wait until all of the interface elements are created.
        * */

    }


    public void createSubscriptions(){
        mainPipeline.getBoneCondition().subscribe(item -> {reportBoneCondition(item);});
        mainPipeline.getSimilarNormal().subscribe(item -> {updateNormalList(item);});
        mainPipeline.getSimilarAbnormal().subscribe(item -> {updateAbormalList(item);});
    }

    public void overallProgress(double progress) {

    }

    public void statusUpdate(@NotNull String message) {

    }

    public void reportBoneCondition(@NotNull Uncertain<BoneCondition> boneCondition) {
        informationPanelController.setBoneCondition(boneCondition);
    }

    public void preprocessedUserImage(@NotNull BufferedImage image) {

    }

    public void overlay(@Nullable BufferedImage matchedNormal, @Nullable BufferedImage matchedAbnormal) {

    }

    public void success(@Nullable BufferedImage matchedNormal, @Nullable BufferedImage matchedAbnormal) {

    }

    public void updateNormalList(List<Rated<ImageSample>> imageSampleList) {
        mainController.setNormalList(imageSampleList);
    }

    public void updateAbormalList(List<Rated<ImageSample>> imageSampleList) {
        mainController.setAbnormalList(imageSampleList);
    }



    public void setInformationPanelController(InformationPanelController informationPanelController) {
        this.informationPanelController = informationPanelController;
    }

    public void setAnalysisController(AnalysisController analysisController) {
        this.analysisController = analysisController;
    }

    public void addImageExplorerController(ImageExplorerController imageExplorerController) {
        this.imageExplorerControllers.add(imageExplorerController);
    }

    public void setMatchListController(MatchListController matchListController) {
        this.matchListController = matchListController;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setUploadController(UploadController uploadController) {
        this.uploadController = uploadController;
    }
}
