package uk.ac.cam.cl.bravo.gui;

enum View {
    INPUT("Input Image"), NORMAL("Best Match, Normal"), ABNORMAL("Best Match, Abormal"), ABNORMAL_OVER("Overlay, Abormal"), NORMAL_OVER("Overlay, Normal");

    private final String string;

    View(String string) {
        this.string  = string;
    }

    @Override
    public String toString() {
        return string;
    }
}
