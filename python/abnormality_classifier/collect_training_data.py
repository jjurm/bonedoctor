import csv
import imageio
import numpy as np
import cv2

# removes filename from a path
def remove_filename(p):
    output = ""
    file_gone = False
    for c in reversed(p):
        if file_gone:
            output += c
        else:
            if c == '/':
                output += c
                file_gone = True
    return output[::-1]

# get all the training data
print('geting training data')
paths = open("MURA-v1.1/train_image_paths.csv", newline='')
images = []
for path in csv.reader(paths):
    images.append(imageio.imread(path[0]))

# find how many images are in each study
print('finding how many images are in each study')
study_to_n = {}
paths_to_training_images = open("MURA-v1.1/train_image_paths.csv", newline='')
for training_image_path in csv.reader(paths_to_training_images):
    study = remove_filename(training_image_path[0])
    if study in study_to_n:
        study_to_n[study] += 1
    else:
        study_to_n[study] = 1

# get labels for training data
print('getting labels for training data')
labels_csv = open("MURA-v1.1/train_labeled_studies.csv", newline='')
labels = []
for label in csv.reader(labels_csv):
    study = label[0]
    n = study_to_n[study]
    for i in range(n):
        labels.append(label[1])

# preprocess images to all be of shape (320, 320, 3)
print('preprocessing images to all be of shape (320, 320, 3)')
for i in range(len(images)):
    img = images[i]
    resized_img = cv2.resize(img, (320, 320))
    if len(img.shape) == 2:
        resized_img = np.dstack([resized_img, resized_img, resized_img])
    images[i] = resized_img

# stack all images into 4D numpy array (tensor)
print('stacking all images into 4D numpy array (tensor)')
for i in range(len(images)):
    img = images[i]
    images[i] = img[np.newaxis, ...]
images_tensor = np.concatenate(images, axis=0)

# save training data
print('saving training data')
with open('training_images', 'wb') as f:
    np.save(f, images_tensor)

# saving labels
print('saving labels')
with open('training_labels', 'wb') as f:
    np.save(f, labels)
