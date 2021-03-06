package uk.ac.cam.cl.bravo.dataset

enum class BoneCondition(val label: String) {
    NORMAL("negative"), ABNORMAL("positive");

    companion object {
        private val map: Map<String, BoneCondition> = values().map { it.label to it }.toMap()

        fun fromLabel(label: String) = map[label]
    }
}
