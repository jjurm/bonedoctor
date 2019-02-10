"""
Script to arrange the MURA-v1.1 dataset images by class into specific folders containing all possible views.

Resulting structure:

- body_part_1
	- img1.png
	- img2.png
	- ...

- body_part_2
	- img1.png
	- img2.png
	- ...

etc.
"""
import os
from collections import defaultdict


def order_by_classes(filepaths_csv):
	"""
	Given a file path csv from the dataset, move to folders labelled by their class names.
	Each image will be identified by a unique identifier.
	"""
	# Map class to image name
	class_to_image_name_dict = defaultdict(list)

	# Get all filepaths in each line
	with open(filepaths_csv, 'r') as file:
		lines = file.readlines()

	# Get images and map to each body part
	for file in lines:
		file_path = os.path.join(DATAROOT, file.strip())
		body_part = file.split("/")[2]
		class_to_image_name_dict[body_part].append(file_path)

	# Start moving the files
	for body_part in class_to_image_name_dict:
		# Create output folder labelled by body part
		output_folder = os.path.join(OUTPUT_DIR, body_part)

		if not os.path.exists(output_folder):
			os.makedirs(output_folder)

		for file in class_to_image_name_dict[body_part]:
			# Get last 4 dirname + filename as identifier
			unique_file_identifier = "_".join(file.split("/")[-5:]) 
			new_filepath = os.path.join(output_folder, unique_file_identifier)

			os.rename(file, new_filepath)


if __name__ == "__main__":
	DATAROOT = "../data"
	OUTPUT_DIR = "../data/MURA-v1.1/image_by_class"

	if not os.path.exists(OUTPUT_DIR):
		os.makedirs(OUTPUT_DIR)

	FILEPATHS_CSV = "../data/MURA-v1.1/train_image_paths.csv"
	order_by_classes(filepaths_csv=FILEPATHS_CSV)