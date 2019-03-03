/home/kwotsin/tensorflow_GTT/tensorflow/bazel-bin/tensorflow/tools/graph_transforms/transform_graph \
--in_graph=./InceptionV3.pb \
--out_graph=./InceptionV3.pb \
--inputs='input_1' \
--outputs='mixed10/concat' \
--transforms='
  strip_unused_nodes(type=float, shape="1,299,299,3")
  fold_batch_norms
  fold_old_batch_norms'