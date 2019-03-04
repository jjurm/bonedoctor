# Bone Doctor

A Part IB Group Project, created for TPP for the **Computer Science Tripos** at the **University of Cambridge**.

## Problem Statement

Large databases of labelled X-ray images such as the MURA dataset of broken bones can be used to train AI systems for medical advice. An intelligent clinical assistant should be able to take a previously unseen X-ray image and link it directly to cases that can guide possible treatment. Your task is to train a neural network that will find a variety of other cases that are clinically similar, but visually distinct, presented in a visual overlay that highlights local differences for a clinician to review. 

## Getting Started

1. Ensure you have Java 11 installed and the `JAVA_HOME` environment variable points to it
2. `git clone git@github.com:jjurm/bonedoctor`
3. Download the MURA dataset and unzip it to `MURA-v1.1` inside the project directory

### Running the Preprocessing

`./gradlew preprocess --args 'all'`, or specify the tasks to run as in `./gradlew preprocess --args 'images matcher'`

Pre-processing is a prerequisite for running the application and consists of the following:
* Pre-processing the images in the dataset (task `images`)
* Calculating hashes of the images for fast lookup of similar images (task `matcher`)

### Running the Application

`./gradlew run` (or set your IDE appropriately)

## The Graphical User Interface

##### Primary Contributor: Ane K Espeseth

Using requirements analysis, we have arrived at a minimum set of features that need to be implemented. These include: 
* X-Ray selection screen: Select an image from the local computer to start the analysis and image matching process. 
* X-Ray analysis result screen: Display the analysis result (normal/abnormal or broken/not broken), a confidence level, and the best matching case from the data set. 
* X-Ray discovery, comparison and inspection screen: Explore the uploaded image (original/pre-processed), the matching X-Ray (side by side/overlay), and the other top image matches (list of images - new selected image replaces the top match in the inspection pane). Zooming and panning functions. This has now been merged together with the result analysis screen. 
* Image export screen: Export image resulting from one or more of the back-end image processing tools - the original image pre-processed for better bone visibility, a side by side comparison of X-Rays or overlaid images. 
* New data set item upload screen:  Add a new X-Ray image together with a case file / other relevant files which can be helpful to the user. Selection menus for parameters which will aid the algorithms' searches, most importantly which body part is displayed.

JavaFx has been used to create the various components of the user interface, enabling rearrangement or elements for rough prototyping, and detailed CSS styling of elements for the later stages of the process.  

The main user interface for our product contains an image display for the user input image. Here the user can zoom and pan around the image for exploration. The image information (including analysis results and confidence) and options - for uploading to the dataset or exporting the processed image - will also be available below in a separate frame. In the image frame is a button that allows the user to switch between the original input image and the one enhanced by pre-processing. To the left of the input image display, the user can choose to add up to two more image exploration screens for convenient image comparison. Here they can select which category of images they want to be displayed - Input, Best Match Normal, Best Match Abnormal, Overlay Input/Normal or Overlay Input/Abnormal. Furthermore, if a Best Match option is selected, the user can pick their desired image in that category in the action frame below, from a list of the highest scoring matches. 

## How Does it Work?

### The Computation Pipeline

<img src="https://github.com/jjurm/bonedoctor/blob/master/images/read/pipeline.png" width="500" height="400"/>

### 1. Image Preprocessing

##### Primary Contributor: Nicole M Joseph

1. It ensures that the background of the X-ray is dark and the bones are relatively lighter.
2. It identifies the border of the image to distinguish between the part of the image containing the X-ray and the background of the image .
3. It increases the contrast between the bones and the background so that the focus of the image is on the bones.
4. It crops the image in order to focus on the body part itself.

By studying the image, a histogram is constructed of frequency of each colour against the colours. The colours are rounded to the nearest 5 so that fluctuations of colours within a grey-scale value of 5 or less is ignored. The background colour is taken to be the (darker) colour at which the change in frequency is greatest. 

Using Sobel’s edge detection, the points of maximum gradient are identified in the image and then these points can be used to construct a convex hull that will enclose the entire image. Here, Graham’s scan is used as the convex hull algorithm. This hull is taken as the border and is used to differentiate between pixels located inside the border (the pixels we care about) and those outside that need only be set to black as part of the background. The hull is stored to be removed after the contrast is altered. 

By using the histogram of colours within the hull a black threshold - the colour below which all pixels must be set to black as the background - and a white threshold - the colour above which all pixels must be set to white as the bones - are established. The remaining pixels’ colours are scaled into this range.The black threshold is taken to be a 5 more than the background value. To calculate the white threshold,the pixels that are lighter than the black threshold are considered, and the lightest colour with an above average frequency is identified and used as the white threshold. 

After this stage a black band is constructed on the convex hull from the edge detection phase. The image is cropped by removing rows and columns from the edges of the image that are completely black (grey-scale value of 0).

### 2. Clustering of Images

##### Primary Contributor: Kwot Sin Lee

In order to facilitate the downstream processing of the images for greater performance, the images could be filtered by finding the views in which they were first taken. Since the MURA dataset has only binary labels of 1 and 0 indicating a presence and absence of a fracture respectively, an unsupervised learning approach is taken. Unsupervised learning in this context is the most _scalable_ way to perform this task, instead of handcrafting feature detectors or manually labelling the images and then performing supervised learning. Specifically, we want to find clusters of images such that given a new image, we can know which cluster it belongs to, and so apply the corresponding algorithms on these filtered set of images belonging to that cluster.

An InceptionV3 network is used to act as an image encoder that takes in an image and produce high level features of the image. This helps to extract the most significant features and represent them in a vector form suitable for clustering. Congruent with standard research practice, we first use model weights pre-trained on the ImageNet dataset for object recognition, which gives us a model capable of understanding high level features such as edges, colours and textures. Subsequently, for each input image,  take the mixed_10 layer output of the network, and ignore the last fully-connected layer output. This produces a _8 x 8 x 2048_ shaped output tensor, which we subsequently reshape into a vector of shape _131072 x 1_ representing the high level features of this one image.

Through performing this inference step for all images, we effectively obtain an _(N,M)_ matrix, where _N_ represents the number of all images used and _M = 131072_ represents the dimensions of each feature vector. Standard K-means clustering is then applied to cluster this feature matrix, producing 2 distinct clusters (see Figure 1 and Figure 2 in test results).

Finally, in order to know what cluster an image belongs to, we perform inference on the image to extract its high level features and compare the L2 distance to the centroids of the clusters obtained. The closest L2 distance to one cluster label will determine which set of images the new image belongs to.

### 3. Classification of Normal/ Abnormal

##### Primary Contributor: Leon Mlodzian

A binary classification model was built using Keras to distinguish normal and abnormal X-ray images. The model used in the MURA paper (2017 Rajpurkar et al.) was implemented, the main component of which was a 169-layer DenseNet (2016 Huang et al.). The final layer of the DenseNet was replaced by a single-output fully connected layer with a sigmoid activation function. The model was trained using the Adam optimizer with a learning rate of 0.0001. The loss function used was weighted binary crossentropy. Training was performed via Google Colab, a free cloud computing service with GPU support. Due to the associated RAM constraint of around 12 GB, each bodypart had to be trained on individually, one after the other. The GPU memory contraint allowed for a batch size of 11. 3 epochs were run for each bodypart's subset of the MURA dataset before moving on to the next bodypart. 6 of these "bodypart-wise" iterations through the MURA dataset were performed essentially yielding 18 epochs of training in total on the entire dataset. The model was then evaluated on the validation dataset and achieved an accuracy of 70.2%. 

A binary classification model was built using Keras to distinguish normal and abnormal X-ray images. The model used in the MURA paper (2017 Rajpurkar et al.) was implemented, the main component of which was a 169-layer DenseNet (2016 Huang et al.). The final layer of the DenseNet was replaced by a single-output fully connected layer with a sigmoid activation function. Python scripts were written to load the dataset and labels, initialise the model with pre-trained weights based on the ImageNet dataset, carry out the training and save the results. In order to integrate the classifier written in Python into the main Java project the following was done. A Python script was used to convert the TensorFlow graph associated with the trained model into a protobuf file, from which the TensorFlow graph can be reconstructed by Java at runtime. Reconstruction of the graph and classification of a new image using the graph in Java were implemented.

### 4. Searching for Matching Images

#### Stage 1

##### Primary Contributor: Shehab Alshehabi

Given an input image, we would like to quickly locate any similar images in order to overlay them. The MURA dataset contains just 40,000 X-Rays and contains over 3 GB of data. It is clearly unfeasible to scan them all. The approach taken in our program is to compress the images into a format that can still be searched for image similarity. To do this, we used a perceptual hash.

We have implemented a ImageHasher class which can take in images as input and produce the closest match as output. The class allows you to choose a higher quality search at the expense of it taking more time.

The image hasher class uses an implementation of the average hash perceptual hashing algorithm in order to hash the images. We tried several algorithms but the average hash algorithm gave us the best balance between speed and accuracy. As the search time was very low, this allowed us to search for rotated versions of the same image.

The pixel hashing approach was inspired by image hashing. Instead of taking an image as an input, we compared each pixel to its surrounding pixels and encoded the results of this comparison as a long. We did this in such a way that we could easily test for rotated versions of the same encoding were just bit shifts of each other and thus could easily be found. We also optimised the match search so that in the case where a match with a Hamming distance of $0$ was required, it would be found with a lookup into a hashmap instead of an iteration through all hashes.

#### Stage 2

##### Primary Contributor: Kwot Sin Lee

A precise image matching algorithm is created to further refine the matches obtained, based on visual similarity. As each image has a corresponding encoded image vector, we find the visual similarity amongst images by computing the cosine similarity between any two vectors. Through computing cosine similarity, we could compare how similar the image vectors are in the high dimensional space they are encoded in, with 1.0 being the best score and -1.0 being the worst (i.e. the two vectors are pointing in diametrically opposite directions).

### 5. Overlaying of Images

##### Primary Contributor: Juraj Micko

The goal of this part of the pipeline was, given a base image and a sample image, to overlay them so that differences can be highlighted and presented to the user.

We try to deform a given sample image to match and overlay the base as much as possible. The procedure is to minimise an _overlay function_ by adjusting the parameters of the transformations, using the BOBYQA minimising algorithm.

The following transformations on the sample image are implemented:
* Affine transform - 6 degrees of freedom (matrix _2 x 2_, plus _2_ parameters for translation)
* Warp transform - Constructs a grid of _6 x 6_ points (see the images in the next section). The outer points are fixed at their position, and the remaining _4 x 4_ points are flexible (each having _2_ degrees of freedom). The source image is then transformed using the Warp transform, from a regular source grid to the given target grid.

Multiple transforms can be used simultaneously.

Given parameters for a list of transformations, the transformations are applied in the given order to the sample image and the resulting transformed image is then compared to the base image. The comparison assigns a score to the result of the overlay - the better match between the base and the sample image, the lower value. For this, the following _overlay functions_ are used to evaluate how good the overlay is.
* Pixel similarity function - determines how similar two images are. Compares corresponding pixels in the two images.
* Parameter penalty function - the more significant the deformation is, the more penalty we get for the given set of parameters.

We composed a function that takes a set of parameters and returns a score. The algorithm then utilizes the Powell's BOBYQA minimisation algorithm to minimise this composite _overlay function_ and returns the parameters of the best transformation found.
It would be better to use a Gradient descent optimisation. However, it is very difficult to compute the gradient of the function to minimise. This is mostly because images are given in terms of discrete pixel values and not mathematical functions, and the gradient computation would then be very inefficient. The BOBYQA algorithm only uses function values to minimse the function, with the drawback that it needs more computing resources to find the minimum.
Each overlay algorithm has multiple parameters, such as thresholds, scales and a multiplicative constants. These parameters have been fine-tuned to bring the best results for the majority of images.

In our algorithms, there is a trade-off between performance and precision. We allow the user to input a 'precision' parameter that specifies the precision level (higher values take longer to compute).
To allow faster computation of the overlay, we allow the precision to affect the resolution of the image that the algorithm works with. With lower precision, the input image is down-scaled first to allow faster minimisation. The drawback is lower accuracy and a worse match between the original base and sample images.
The 'precision' argument also affects the 'Stopping trust region radius' of the BOBYQA algorithm, which specifies the target precision of the arguments of the minimised function.

### 6. Highlighting irregularities

##### Primary Contributor: Shehab Alshehabi

The goal of this stage is to help clinicians identify where a fracture could potentially be located. This was a part of our project that we originally dismissed as being too difficult to implement. However, after using the hashing approach to find matching images, we realised that it could be extented to highlight fractures. We do this by hashing each pixel and some parts of it's surroundings. In this way we can find pixels that do not have another similar pixel in an sample healthy images and highlight them. In some cases this works very well automatically but in others the user is required to do tune two parameters which define what is considered similar.

## Authors

* **Ane Espeseth** ([anespz](https://github.com/anespz))
* **Juraj Micko** ([jjurm](https://github.com/jjurm))
* **Kwot Sin Lee** ([kwotsin](https://github.com/kwotsin))
* **Leon Mlodzian** ([LeonMlo](https://github.com/LeonMlo))
* **Nicole Joseph** ([nmj33](https://github.com/nmj33))
* **Shehab Alshehabi** ([shehabalshehabi](https://github.com/shehabalshehabi))
