import tensorflow as tf
from tensorflow.python.framework import graph_util
from keras.applications.inception_v3 import InceptionV3
from keras import backend as K
from keras.applications.densenet import DenseNet169
from keras.layers import Dense, Flatten
from keras.models import Sequential
import numpy


def freeze_model(sess, model, output_dir, output_filename):
    """
    Given a keras model, freezes the model weights and outputs it into
    a protobuf file. Output nodes are also fed in from the keras model.
    For consistency with Java API, we want to save input and output tensor
    names as well.
    """
    # Extract graph from the session
    graph = sess.graph

    # Obtain the output nodes from the model
    output_node_names = [out.op.name for out in model.outputs]

    # Obtain all variables to be frozen into constants.
    with graph.as_default():
        # # Setup output nodes
        # # TODO: Check if assumes more than 1 output nodes.
        # output_node_names += [v.op.name for v in tf.global_variables()]

        # Get graph def
        input_graph_def = graph.as_graph_def()

        # Freeze graph with ALL variables.
        frozen_graph = tf.graph_util.convert_variables_to_constants(
            sess=sess,
            input_graph_def=input_graph_def,
            output_node_names=output_node_names)

        # Write to pb file
        print("INFO: Writing graph to be frozen...")
        tf.train.write_graph(frozen_graph,
                             logdir=output_dir,
                             name=output_filename,
                             as_text=False)

        print(output_node_names)

densenet = DenseNet169(include_top=False, weights=None, input_tensor=None, input_shape=(320,320,3), pooling=None, classes=False)
model = Sequential()
model.add(densenet)
model.add(Flatten())
model.add(Dense(1, activation='sigmoid'))
model.load_weights('weights-01-1.00.hdf5')

frozen_graph = freeze_model(
    sess=K.get_session(),
    model=model,
    output_dir=".",
    output_filename="BoneConditionClassifier.pb")
