from keras.applications.densenet import DenseNet169
from keras.layers import Dense, Flatten
from keras.models import Sequential
from keras.callbacks import ModelCheckpoint
import numpy

# load data
print('loading data')
training_images = numpy.load('training_images')
training_labels = numpy.load('training_labels')
validation_images = numpy.load('validation_images')
validation_labels = numpy.load('validation_labels')

# build model and initialise with imagenet pretrained weights
print('building model and initialising weights')
densenet = DenseNet169(include_top=False, weights=None, input_tensor=None, input_shape=(320,320,3), pooling=None, classes=False)
densenet.load_weights('densenet169_weights_tf_dim_ordering_tf_kernels_notop.h5')
model = Sequential()
model.add(densenet)
model.add(Flatten())
model.add(Dense(1, activation='sigmoid'))

# loss function
normal_fraction =
abnormal_fraction =
def weighted_binary_crossentropy(y_true, y_pred):
    return - normal_fraction * y_true * math.log(y_pred) - abnormal_fraction * (1 - y_true) * math.log(1 - y_pred)

# compile model
print('compiling model')
model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])

# set checkpoints
print('setting checkpoints')
filepath="weights-{epoch:02d}-{val_acc:.2f}.hdf5"
checkpoint = ModelCheckpoint(filepath, monitor='val_acc', verbose=1, save_best_only=False)
callbacks_list = [checkpoint]

# fit model and evaluate on validation dataset
print('fitting model')
model.fit(x=training_images, y=training_labels, validation_data=(validation_images, validation_labels), callbacks=callbacks_list, epochs=3, batch_size=8)
