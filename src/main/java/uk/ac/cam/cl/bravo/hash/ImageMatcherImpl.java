package uk.ac.cam.cl.bravo.hash;

import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;
import uk.ac.cam.cl.bravo.dataset.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageMatcherImpl implements Serializable, ImageMatcher {

    private HashMap<BoneCondition, HashMap<Bodypart, HashMap<BodypartView, ImageHasher>>> imageHashers;

    public List<Pair<File, Integer>> findMatchingImage(@NotNull BufferedImage image, @NotNull BoneCondition boneCondition, @NotNull BodypartView bodypartView, int n) {
        ImageHasher imageHasher = imageHashers.get(boneCondition).get(bodypartView.getBodypart())
                .get(bodypartView.getValue());

        return imageHasher.getNPairs(image, n);
    }

    public static ImageMatcherImpl getImageMatcher(File f){
        try {
            FileInputStream fileInputStream = new FileInputStream(f);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            return (ImageMatcherImpl) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static void trainImageMatcher(File f){
        try{

        } catch (Exception e){

        }
    }

    private ImageMatcherImpl(){
        imageHashers = new HashMap<>();

        for (BoneCondition boneCondition : BoneCondition.values()){
            for (Bodypart bodypart : Bodypart.values()){
                //for (BodypartView bodypartView : BodypartView.values()){

                try {
                    Dataset dataset = new Dataset();
                    Map<String, ImageSample> m = dataset.getTraining();
                    m.values();
                } catch (IOException e){
                    System.out.println(e.getMessage());
                }


                //}
            }
        }


        for (Bodypart bodypart:Bodypart.values()) {
            ImageHasher a = new ImageHasher();

            File category = new File("train/XR_"+bodypart);

            System.out.println("Starting "+category.getName());

            File[] patientFiles = category.listFiles();
            if (patientFiles != null) {
                for (File patientFile : patientFiles) {
                    File[] photoFiles = patientFile.listFiles();
                    if (photoFiles != null) {
                        for (File photoFile : photoFiles) {
                            File[] imageFiles = photoFile.listFiles();
                            if (imageFiles != null) {
                                for (File image : imageFiles) {
                                    a.addImageFile(image);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args){
        ImageMatcherImpl imageMatcher = new ImageMatcherImpl();
    }
}
