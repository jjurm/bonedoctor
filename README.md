# Bone Doctor

A Part IB (second year) Group Project, created for TPP for the **Computer Science Tripos** at the **University of Cambridge**.

## Problem statement

Large databases of labelled X-ray images such as the MURA dataset of broken bones can be used to train AI systems for medical advice. An intelligent clinical assistant should be able to take a previously unseen X-ray image and link it directly to cases that can guide possible treatment. Your task is to train a neural network that will find a variety of other cases that are clinically similar, but visually distinct, presented in a visual overlay that highlights local differences for a clinician to review. 

## Getting Started

1. Ensure you have Java 11 installed and the `JAVA_HOME` environment variable points to it
2. `git clone git@github.com:jjurm/bonedoctor`
3. Download the MURA dataset and unzip it to `MURA-v1.1` inside the project directory

### Running the pre-processing

`./gradlew preprocess --args 'all'`, or specify the tasks to run as in `./gradlew preprocess --args 'images matcher'`

Pre-processing is a prerequisite for running the application and consists of the following:
* Pre-processing the images in the dataset (task `images`)
* Calculating hashes of the images for fast lookup of similar images (task `matcher`)

### Running the application

`./gradlew run` (or set your IDE appropriately)

## Authors

* **Ane Espeseth** ([anespz](https://github.com/anespz))
* **Juraj Micko** ([jjurm](https://github.com/jjurm))
* **Kwot Sin Lee** ([kwotsin](https://github.com/kwotsin))
* **Leon Mlodzian** ([LeonMlo](https://github.com/LeonMlo))
* **Nicole Joseph** ([nmj33](https://github.com/nmj33))
* **Shehab Alshehabi** ([shehabalshehabi](https://github.com/shehabalshehabi))
