"""
Script for post processing results from python ETL pipeline for use in Java API.
"""
import json
import os
import pickle as pkl
import re


def convert_to_java_readable_format(feat_file, output_dir):
	"""
	Converts the pkl file into Java suitable format
	"""

	# Load pickle object
	with open(feat_file, 'rb') as file:
		features = pkl.load(file)

	# Write by label major
	for label in features:
		file_to_write = os.path.join(output_dir, "mean_features_label_{}.txt".format(label))
		with open(file_to_write, 'w') as file:
			for k in features[label]:
				file.write(str(k) + "\n")


def swap_keys(json_file):
	"""
	Given a json file mapping label to a list of image filenames, get an output json mapping
	each image filename to a label instead.
	"""
	# Load the json file object
	with open(json_file, 'r') as file:
		labels_to_image_filenames_dict = json.load(file)

	# Convert into relative paths and swap key and values
	image_filename_to_label_dict = {}

	for label, image_filenames in labels_to_image_filenames_dict.items():
		for filename in image_filenames:
			# Swap keys and labels
			image_filename_to_label_dict[filename] = label

	# Output the file
	output_file = os.path.join(OUTPUT_DIR, BODY_PART, "label_to_image_filenames/image_filename_to_label_dict.json")
	with open(output_file, 'w') as file:
		json.dump(image_filename_to_label_dict, file)


def restore_paths(json_file):
	with open(json_file, 'r') as file:
		labels_to_image_filenames_dict = json.load(file)

	for label in labels_to_image_filenames_dict:
		image_filenames = labels_to_image_filenames_dict[label]

		for i in range(len(image_filenames)):
			try:
				m = re.search(r"(.*)_(XR_.*)_(patient.*)_(study.*)_(image.*)", image_filenames[i])
				image_filenames[i] = "/".join(m.groups())
			except:
				continue
				
	# Output the file
	with open(json_file, 'w') as file:
		json.dump(labels_to_image_filenames_dict, file)


if __name__ == "__main__":
	OUTPUT_DIR = "../output"

	for BODY_PART in os.listdir(OUTPUT_DIR):
		print("Processing {} folder...".format(BODY_PART))
		json_file = os.path.join(OUTPUT_DIR, BODY_PART, "label_to_image_filenames/labels_to_image_filenames.json")

		# Perform path restoration
		restore_paths(json_file)

		# Perform the swapping of keys
		swap_keys(json_file)

		# Perform the data conversion
		output_json_dir = os.path.join(OUTPUT_DIR, BODY_PART, "label_to_image_filenames")
		feat_file = os.path.join(output_json_dir, "labels_to_image_mean_feat.pkl")

		convert_to_java_readable_format(feat_file, output_json_dir)

