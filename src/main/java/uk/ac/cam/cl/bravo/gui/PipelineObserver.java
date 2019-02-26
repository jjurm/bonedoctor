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

    private UploadController uploadController;
    private ImageExplorerController imageExplorerController;
    private MatchListController matchListController;
    private AnalysisController analysisController;
    private MainController mainController;

    private MainPipeline mainPipeline;

    public PipelineObserver() {
        mainPipeline.getBoneCondition().subscribe(item -> {reportBoneCondition(item);});
        mainPipeline.getSimilarNormal().subscribe(item -> {updateNormalList(item);});
        mainPipeline.getSimilarAbnormal().subscribe(item -> {updateAbormalList(item);});
    }

    public void overallProgress(double progress) {

    }

    public void statusUpdate(@NotNull String message) {

    }

    public void reportBoneCondition(@NotNull Uncertain<BoneCondition> boneCondition) {

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
        mainController.setAbormalList(imageSampleList);
    }

    public void addUploadController(UploadController ctrl) {
        uploadController = ctrl;
    }

    public void addAnalysisController(AnalysisController ctrl) {
        analysisController = ctrl;
    }

    public void addImageExplorerController(ImageExplorerController ctrl) {
        imageExplorerController = ctrl;
    }

    public void addMainController(MainController ctrl) {
        mainController = ctrl;
    }

    public void addMatchListController(MatchListController ctrl) {
        matchListController = ctrl;
    }

}
