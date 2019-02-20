package uk.ac.cam.cl.bravo.gui;

import javafx.css.Match;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.ac.cam.cl.bravo.MainPipelineObserver;
import uk.ac.cam.cl.bravo.dataset.BoneCondition;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PipelineObserver implements MainPipelineObserver {

    private UploadController uploadController;
    private ImageExplorerController imageExplorerController;
    private MatchListController matchListController;
    private AnalysisController analysisController;
    private MainController mainController;

    @Override
    public void overallProgress(double progress) {

    }

    @Override
    public void statusUpdate(@NotNull String message) {

    }

    @Override
    public void reportBoneCondition(@NotNull BoneCondition boneCondition) {

    }

    @Override
    public void preprocessedUserImage(@NotNull BufferedImage image) {

    }

    @Override
    public void partialOverlay(@Nullable BufferedImage matchedNormal, @Nullable BufferedImage matchedAbnormal) {

    }

    @Override
    public void success(@Nullable BufferedImage matchedNormal, @Nullable BufferedImage matchedAbnormal) {

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
