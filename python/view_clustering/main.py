"""
Script to extract features from MURA dataset and perform clustering.

Clustering performance as measured by silhouette score.
"""
import os
import numpy as np
import pickle as pkl
import json
from collections import defaultdict

import matplotlib.pyplot as plt
from scipy.misc import imread
from sklearn import cluster
from sklearn.metrics import silhouette_score

import tensorflow as tf
import keras as K
from keras import layers
from keras.models import Sequential
from keras.applications.inception_v3 import InceptionV3
from keras.applications.inception_v3 import preprocess_input
from keras.preprocessing import image


flags = tf.app.flags

flags.DEFINE_string(
    "images_dir",
    "./data/MURA-v1.1/image_by_class/",
    "Path to folder containing all images of one body part class.")

flags.DEFINE_string(
    "body_part",
    "XR_HUMERUS",
    "Choose from [XR_SHOULDER, XR_FOREARM, XR_WRIST, XR_ELBOW, XR_FINGER, XR_HAND, XR_HUMERUS].")

flags.DEFINE_string(
    "output_dir",
    "./output",
    "Path to folder containing output objects such as image features.")

flags.DEFINE_integer(
    "limit",
    None,
    "The number of images to limit the inference to. Useful for testing the pipeline with small no. of imgs.")

flags.DEFINE_integer(
    "num_clusters",
    2,
    "Number of clusters to use for grouping the extracted image features")

flags.DEFINE_boolean(
    "overwrite",
    False,
    "If True, performs inference again and overwrites the cached predicted if they exist.")

flags.DEFINE_boolean(
    "visualise",
    False,
    "If True, visualises the images for each cluster")

FLAGS = flags.FLAGS


def build_model():
    """
    Builds InceptionV3 Model with pre-trained weights.
    """

    model = InceptionV3(include_top=False, weights='imagenet')

    return model


def extract_features(model,
                     images_dir,
                     image_features_file,
                     image_features_names_file,
                     overwrite=False):
    """
    Passes each image from images_dir into the model and produces the 1D features. 
    Features are then stacked to produce a 2D matrix of features that could be clustered.
    The results are then cached in image_features_file.

    Skips the feature extraction if it was already done, unless overwrite is True.

    Args:
        - model(Model): Keras model object for prediction
        - images_dir(str): Path to preprocessed images directory.
        - image_features_file(str): Path to where the image features would be stored in npy format
                                    after inference is completed.
        - image_features_names_file(str): Path to where the image filenames corresponding to the image
                                         features, in sorted order, are stored. Important for locating
                                         the correct images to the features used for prediction.
        - overwrite(bool): If True, then overwrites the existing image feature files by performing
                           inference again.
    
    Returns:
        - image_features(nd array): numpy array of shape (N, M) where N is the number of examples
                                    and M is the number of features per example, as extracted from
                                    the pre-trained model.

        - image_features_names(list): A list of image filenames corresponding in index to features in
                                      image_features.
    """
    # Load cached image features
    if os.path.exists(image_features_file)\
       and os.path.exists(image_features_names_file)\
       and not overwrite:
        print("INFO: Loading existing image features and their corresponding names files...")
        with open(image_features_names_file, "rb") as file:
            image_features_names = pkl.load(file)

        image_features = np.load(image_features_file)

        return image_features, image_features_names

    else:
        # Map image filenames to np array before stacking them
        image_to_features_dict = {}

        # Get image filenames with FLAGS.limit if required.
        image_filenames = os.listdir(images_dir)
        if FLAGS.limit:
            image_filenames = image_filenames[:FLAGS.limit]
        image_filenames = [file for file in image_filenames if file.endswith('.png')]

        # Iterate through the images and extract the features
        for i, image_filename in enumerate(image_filenames):
            print("INFO: Performing inference on {}/{} images".format(i+1, len(image_filenames)))

            # Read the image into np array
            img = image.load_img(os.path.join(IMAGES_DIR, image_filename), target_size=IMAGE_SIZE)
            img = image.img_to_array(img)
            img = np.expand_dims(img, 0) # Expands the first dimension to get shape (1, 299, 299, 3)

            # Preprocess data and pass through model
            img = preprocess_input(img)
            img_features = model.predict(img)

            # Flatten the input to 1d vector representing all the features
            # Useful for stacking features later
            img_features = img_features.flatten()

            # Store image names and features
            image_to_features_dict[image_filename] = img_features

        # Sort by image name before stacking. Useful for visualising later.
        image_to_features_items = sorted(image_to_features_dict.items(), key=lambda x: x[0])
        image_features = np.vstack([item[1] for item in image_to_features_items])
        image_features_names = [item[0] for item in image_to_features_items]

        # Cache the image features and corresponding names
        np.save(image_features_file, image_features)
        with open(image_features_names_file, 'wb') as file:
            pkl.dump(image_features_names, file)

        return image_features, image_features_names


def predict_and_eval(image_features,
                     image_features_names,
                     num_clusters=2,
                     output_names_file=None,
                     output_feats_file=None):
    """
    Cluster by image features using K-means.

    Args:
        - image_features(nd array): Numpy array of shape (N, M) where N is the number of examples
                                    and M is the number of features per example, as extracted from
                                    the pre-trained model.
        - image_features_names(list): A list of image filenames corresponding in index to features in
                                      image_features.
        - num_clusters(int): The number of clusters to use to group the image features.
        - output_names_file(str): The output json file to write the results (label mapping to image filenames) to.

    Returns:
        - pred(nd array): Numpy array of shape (N,), representing cluster labels ordered by index corresponding
                          to image_features.

        -label_to_image_filenames_dict(dict): A hash table mapping cluster labels to a list of image filenames.

    """
    print("INFO: Clustering features with {} clusters now...".format(num_clusters))
    cluster_model = cluster.KMeans(n_clusters=num_clusters)
    pred = cluster_model.fit_predict(image_features)


    print("INFO: Silhouette Score ({} clusters): {}".format(
        num_clusters, silhouette_score(image_features, pred)))

    # Map labels to the image filenames and image_features
    label_to_image_filenames_dict = defaultdict(list)
    label_to_image_features_dict = defaultdict(list)
    for i in range(pred.shape[0]):
        # Change pred label to string for json dumping
        label_to_image_filenames_dict[str(pred[i])].append(image_features_names[i])

        label_to_image_features_dict[pred[i]].append(image_features[i])

    if output_names_file:
        print("INFO: Saving labels to image filenames output...")
        with open(output_names_file, 'w') as file:
            json.dump(label_to_image_filenames_dict, file)

    # Compute the average features for each cluster
    label_to_mean_features_dict = {}
    for label in label_to_image_features_dict:
        features_list = label_to_image_features_dict[label]

        # Find the mean of all features
        mean_features = sum(features_list) / len(features_list)
        label_to_mean_features_dict[label] = mean_features

    if output_feats_file:
        print("INFO: Saving labels to mean image_features...")
        with open(output_feats_file, 'wb') as file:
            pkl.dump(label_to_mean_features_dict, file)

    return pred, label_to_image_filenames_dict


def vis_images(label_to_image_filenames_dict, num_images_to_show=64):
    """
    Visualises random images from each cluster.

    Args:
        -label_to_image_filenames_dict(dict): A hash table mapping cluster labels to a list of image filenames.
    """

    # Decide number of imgs to be multiples of 8
    # otherwise, truncate to nearest multiple.
    if (num_images_to_show % 8 != 0):
        num_images_to_show = (num_images_to_show / 8) * 8

    # Visualise output of each label
    for label in label_to_image_filenames_dict:
        # Read a fixed number of images
        filenames = label_to_image_filenames_dict[label][:num_images_to_show]
        filenames = [os.path.join(IMAGES_DIR, filename) for filename in filenames\
                     if filename.endswith('.png')]
        images = [imread(filename) for filename in filenames]

        # Visualise them in a plot
        fig = plt.figure(figsize=(8,8))
        num_cols = 8
        num_rows = num_images_to_show / 8

        for i in range(1, min(int(num_cols*num_rows)+1, len(images))):
            fig.add_subplot(num_rows, num_cols, i)
            plt.imshow(images[i-1])
            plt.axis('off')

        plt.axis('off')
        plt.show()


if __name__ == "__main__":
    #=======================
    # Paths and Directories
    #=======================
    # Path to image features storage
    IMAGE_FEATURES_DIR = os.path.join(FLAGS.output_dir, FLAGS.body_part, "features")

    # Path to output json file dir
    OUTPUT_JSON_DIR = os.path.join(FLAGS.output_dir, FLAGS.body_part, "label_to_image_filenames")

    # Path to image directory of body part
    IMAGES_DIR = os.path.join(FLAGS.images_dir, FLAGS.body_part)

    # Create all directories
    DIRECTORIES = [IMAGES_DIR,
                   FLAGS.output_dir,
                   IMAGE_FEATURES_DIR,
                   OUTPUT_JSON_DIR]

    for DIR in DIRECTORIES:
        if not os.path.exists(DIR):
            os.makedirs(DIR)

    # Paths to storing image features and corresponding filenames
    IMAGE_FEATURES_FILE = os.path.join(IMAGE_FEATURES_DIR, FLAGS.body_part + "_features.npy")
    IMAGE_FEATURES_NAMES_FILE = os.path.join(IMAGE_FEATURES_DIR, FLAGS.body_part + "_features_names.pkl")

    # Path to output file mapping view labels to images
    LABELS_TO_IMAGE_FILENAMES_FILE = os.path.join(OUTPUT_JSON_DIR, "labels_to_image_filenames.json")
    LABELS_TO_IMAGE_MEAN_FEAT_FILE = os.path.join(OUTPUT_JSON_DIR, "labels_to_image_mean_feat.pkl")


    #====================
    # Default Parameters
    #====================
    # Image size required by pre-trained network of inception
    IMAGE_SIZE = (299, 299) 


    #==========================
    # Inference and Prediction
    #==========================
    # Build the model
    model = build_model()

    # Extract features and corresponding names
    image_features, image_features_names = extract_features(model=model,
                                                            images_dir=IMAGES_DIR,
                                                            image_features_file=IMAGE_FEATURES_FILE,
                                                            image_features_names_file=IMAGE_FEATURES_NAMES_FILE,
                                                            overwrite=FLAGS.overwrite)

    # Perform prediction
    pred, labels_to_image_filenames_dict = predict_and_eval(image_features=image_features,
                                                            image_features_names=image_features_names,
                                                            num_clusters=FLAGS.num_clusters,
                                                            output_names_file=LABELS_TO_IMAGE_FILENAMES_FILE,
                                                            output_feats_file=LABELS_TO_IMAGE_MEAN_FEAT_FILE)

    # Perform visualisation
    if FLAGS.visualise:
        vis_images(labels_to_image_filenames_dict)