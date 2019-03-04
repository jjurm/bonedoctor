package uk.ac.cam.cl.bravo.gui;

enum View {
    INPUT("Input Image"), NORMAL("Best Match"), NORMAL_OVER("Overlay"), HIGHLIGHT("Highlight irregularities");

    private final String string;

    View(String string) {
        this.string  = string;
    }

    @Override
    public String toString() {
        return string;
    }

    public boolean equals(View view2) {
        return (this.string.equals(view2.toString()));
    }
}
