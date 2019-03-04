#### Stage 2
##### Primary Contributor: Kwot Sin Lee
<img src="https://github.com/jjurm/bonedoctor/blob/master/python/view_clustering/images/filter0.png" width="300" height="300"/>  <img src="https://github.com/jjurm/bonedoctor/blob/master/python/view_clustering/images/filter2.png" width="300" height="300"/>

*Figure 1: Original image input to find matching image.*

*Figure 2: Visually closest image found based on cosine similarity*

A precise image matching algorithm is created to further refine the matches obtained, based on visual similarity. As each image has a corresponding encoded image vector, we find the visual similarity amongst images by computing the cosine similarity between any two vectors. Through computing cosine similarity, we could compare how similar the image vectors are in the high dimensional space they are encoded in, with 1.0 being the best score and -1.0 being the worst (i.e. the two vectors are pointing in diametrically opposite directions).

The above 2 images show a close match based on having the best cosine similarity of a select n images provided from the previous pipeline. The next image shows a bad example with the lowest cosine similarity **despite being in the same cluster**. Thus, using cosine similarity is a good indicator of compute visual similarity of images using their feature vectors.

<img src="https://github.com/jjurm/bonedoctor/blob/master/python/view_clustering/images/filter1.png" width="300" height="300"/>

*Figure 3: Bad matching image in the same cluster due to low cosine similarity score*


### 2. Clustering of Images
<img src="https://github.com/jjurm/bonedoctor/blob/master/python/view_clustering/images/cluster0.png" width="400" height="400"/> <img src="https://github.com/jjurm/bonedoctor/blob/master/python/view_clustering/images/cluster1.png" width="400" height="400"/>

*Figure 1: Cluster label 0 images with mostly flat hand images.*

*Figure 2: Cluster label 1 images with mostly curved hand images.*


##### Primary Contributor: Kwot Sin Lee

In order to facilitate the downstream processing of the images for greater performance, the images could be filtered by finding the views in which they were first taken. Since the MURA dataset has only binary labels of 1 and 0 indicating a presence and absence of a fracture respectively, an unsupervised learning approach is taken. Unsupervised learning in this context is the most _scalable_ way to perform this task, instead of handcrafting feature detectors or manually labelling the images and then performing supervised learning. Specifically, we want to find clusters of images such that given a new image, we can know which cluster it belongs to, and so apply the corresponding algorithms on these filtered set of images belonging to that cluster.

An InceptionV3 network is used to act as an image encoder that takes in an image and produce high level features of the image. This helps to extract the most significant features and represent them in a vector form suitable for clustering. Congruent with standard research practice, we first use model weights pre-trained on the ImageNet dataset for object recognition, which gives us a model capable of understanding high level features such as edges, colours and textures. Subsequently, for each input image,  take the mixed_10 layer output of the network, and ignore the last fully-connected layer output. This produces a _8 x 8 x 2048_ shaped output tensor, which we subsequently reshape into a vector of shape _131072 x 1_ representing the high level features of this one image.

Through performing this inference step for all images, we effectively obtain an _(N,M)_ matrix, where _N_ represents the number of all images used and _M = 131072_ represents the dimensions of each feature vector. Standard K-means clustering is then applied to cluster this feature matrix, producing 2 distinct clusters (see cluster figures above).

Finally, in order to know what cluster an image belongs to, we perform inference on the image to extract its high level features and compare the L2 distance to the centroids of the clusters obtained. The closest L2 distance to one cluster label will determine which set of images the new image belongs to.
