from keras.applications.densenet import DenseNet169
from keras.layers import Dense, Flatten
from keras.models import Sequential
import numpy

# load data
print('loading data')
training_images = numpy.load('training_images')
training_labels = numpy.load('training_labels')
validation_images = numpy.load('validation_images')
validation_labels = numpy.load('validation_labels')

# build model
print('building model')
densenet = DenseNet169(include_top=False, weights=None, input_tensor=None, input_shape=(320,320,3), pooling=None, classes=False)
model = Sequential()
model.add(densenet)
model.add(Flatten())
model.add(Dense(1, activation='sigmoid'))

# compile model
print('compiling model')
model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])

# fit model and evaluate on validation dataset
print('fitting model')
model.fit(x=training_images, y=training_labels, validation_data=(validation_images, validation_labels), epochs=2, batch_size=8)
