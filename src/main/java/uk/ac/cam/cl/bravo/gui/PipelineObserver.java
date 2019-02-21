package uk.ac.cam.cl.bravo.gui;

import javafx.css.Match;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.ac.cam.cl.bravo.dataset.BoneCondition;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PipelineObserver  {

    private UploadController uploadController;
    private ImageExplorerController imageExplorerController;
    private MatchListController matchListController;
    private AnalysisController analysisController;
    private MainController mainController;

    public void overallProgress(double progress) {

    }

    public void statusUpdate(@NotNull String message) {

    }

    public void reportBoneCondition(@NotNull BoneCondition boneCondition) {

    }

    public void preprocessedUserImage(@NotNull BufferedImage image) {

    }

    public void overlay(@Nullable BufferedImage matchedNormal, @Nullable BufferedImage matchedAbnormal) {

    }

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
