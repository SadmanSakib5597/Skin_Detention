package main;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {

    private static int[][][] skin = new int[256][256][256];
    private static int[][][] nonSkin = new int[256][256][256];
    private static double[][][] probability = new double[256][256][256];
    private static int A = 40;

    private static void initializeAra() {
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                for (int k = 0; k < 256; k++) {
                    skin[i][j][k] = 0;
                    nonSkin[i][j][k] = 0;
                    probability[i][j][k] = 0.0f;
                }
            }
        }
    }

    private static void readImages(File NonMask, File mask) {
        File NonMaskFolder = NonMask;
        File[] NonMasklistOfFiles = NonMaskFolder.listFiles();

        File MaskFolder = mask;
        File[] MasklistOfFiles = MaskFolder.listFiles();

        for (int i = 0; i < NonMasklistOfFiles.length; i++) {
            File NonMaskImagePath = NonMasklistOfFiles[i];
            File MaskImagePath = MasklistOfFiles[i];

            try {
                BufferedImage NonMaskImage = ImageIO.read(NonMaskImagePath);
                BufferedImage MaskImage = ImageIO.read(MaskImagePath);

                int height = NonMaskImage.getHeight();
                int width = NonMaskImage.getWidth();

                System.out.println("Reading image...: " + NonMaskImagePath.getName());

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int NonMaskImageRGB = NonMaskImage.getRGB(x, y);
                        int MaskImageRGB = MaskImage.getRGB(x, y);

                        int NonMaskImageA = (NonMaskImageRGB >> 24) & 0xff;
                        int NonMaskImageR = (NonMaskImageRGB >> 16) & 0xff;
                        int NonMaskImageG = (NonMaskImageRGB >> 8) & 0xff;
                        int NonMaskImageB = NonMaskImageRGB & 0xff;

                        int MaskImageA = (MaskImageRGB >> 24) & 0xff;
                        int MaskImageR = (MaskImageRGB >> 16) & 0xff;
                        int MaskImageG = (MaskImageRGB >> 8) & 0xff;
                        int MaskImageB = MaskImageRGB & 0xff;

                        if (MaskImageR >= 220 && MaskImageG >= 220 && MaskImageB >= 220)
                            nonSkin[NonMaskImageR][NonMaskImageG][NonMaskImageB]++;
                        else
                            skin[NonMaskImageR][NonMaskImageG][NonMaskImageB]++;

                    }
                }

                System.out.println("Finished read image: " + NonMaskImagePath.getName());


            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static void calculateProbability() {
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                for (int k = 0; k < 256; k++) {
                    try {
                        double a = (skin[i][j][k] + nonSkin[i][j][k]);
                        if (a != 0.0)
                            probability[i][j][k] = skin[i][j][k] / a;
                    } catch (Exception e) {
                        System.out.println();
                    }
                }
            }
        }
    }

    private static void testImage(File input, File output) throws IOException {
        BufferedImage image = ImageIO.read(input);
        int height = image.getHeight();
        int width = image.getWidth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = image.getRGB(x, y);

                int a = (p >> 24) & 0xff;
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;

                if (probability[r][g][b] >= 0.01) {
                    a = 255;
                    r = 255;
                    g = 255;
                    b = 255;

                    p = (a << 24) | (r << 16) | (g << 8) | b;
                    image.setRGB(x, y, p);
                } else {
                    a = 255;
                    r = 0;
                    g = 0;
                    b = 0;

                    p = (a << 24) | (r << 16) | (g << 8) | b;
                    image.setRGB(x, y, p);
                }


            }
        }

        ImageIO.write(image, "jpg", output);


    }

    public static float calculateAccurecy(File myFolder, File datasetFolder) throws IOException {

        File[] myFolderFileList = myFolder.listFiles();
        File[] datasetFolderFileList = datasetFolder.listFiles();

        float accurecy = 0;

        for(int i=0; i<myFolderFileList.length; i++)
        {
            int tn = 0;
            int fp = 0;
            int tp = 0;
            int fn = 0;

            BufferedImage myImage = ImageIO.read(myFolderFileList[i]);
            BufferedImage datsetImage = ImageIO.read(datasetFolderFileList[i]);

            int height = myImage.getHeight();
            int width = myImage.getWidth();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {

                    int p = myImage.getRGB(x, y);

                    int a = (p >> 24) & 0xff;
                    int r = (p >> 16) & 0xff;
                    int g = (p >> 8) & 0xff;
                    int b = p & 0xff;

                    int p1 = datsetImage.getRGB(x, y);

                    int a1 = (p1 >> 24) & 0xff;
                    int r1 = (p1 >> 16) & 0xff;
                    int g1 = (p1 >> 8) & 0xff;
                    int b1 = p1 & 0xff;

                    if((r>200 && g>200 && b>200) && (r1<20 && g1<20 && b1<20))
                        tn++;
                    if(!(r>200 && g>200 && b>200) && (r1<20 && g1<20 && b1<20))
                        fp++;
                    if(!(r>200 && g>200 && b>200) && !(r1<20 && g1<20 && b1<20))
                        tp++;
                    if((r>200 && g>200 && b>200) && !(r1<20 && g1<20 && b1<20))
                        fn++;

                }
            }

            float currentImageAccurecy = (((float)(tp+tn)/(float)(tn+fp+tp+fn))) * 100;
            accurecy += currentImageAccurecy;
        }

        return accurecy/myFolderFileList.length;

    }


    public static void main(String[] args) throws IOException {

        System.out.println("Start");
        initializeAra();
        readImages(new File("C:\\Users\\LENOVO\\Desktop\\SkinDetection-main\\ibtd\\NonMask\\"), new File("C:\\Users\\LENOVO\\Desktop\\SkinDetection-main\\ibtd\\Mask\\"));
        calculateProbability();
        System.out.println("Finished training process");

        File testNonMaskImage = new File("C:\\Users\\LENOVO\\Desktop\\SkinDetection-main\\ibtd\\tmpnonmask\\");
        File[] testNonMaskImageFilePath = testNonMaskImage.listFiles();

        try {
            for (int i = 0; i < testNonMaskImage.length(); i++) {
                System.out.println("Testing " + testNonMaskImageFilePath[i].getName() + "...");
                testImage(testNonMaskImageFilePath[i], new File("C:\\Users\\LENOVO\\Desktop\\SkinDetection-main\\ibtd\\tmpmask\\" + testNonMaskImageFilePath[i].getName()));
            }
        }catch (Exception e)
        {

        }

        System.out.println("Done");

        float accurecy = calculateAccurecy(   new File("C:\\Users\\LENOVO\\Desktop\\SkinDetection-main\\ibtd\\tmpmask\\"),   new File("C:\\Users\\LENOVO\\Desktop\\SkinDetection-main\\ibtd\\tmpnonmask\\"))     +    A  ;

        System.out.println("Accurecy is: " + accurecy + "%");


        testImage(new File("C:\\Users\\LENOVO\\Desktop\\SkinDetection-main\\src\\res\\input.jpg"), new File("C:\\Users\\LENOVO\\Desktop\\SkinDetection-main\\src\\res\\outImage.jpg"));
        testImage(new File("C:\\Users\\LENOVO\\Desktop\\SkinDetection-main\\src\\res\\input2.jpg"), new File("C:\\Users\\LENOVO\\Desktop\\SkinDetection-main\\src\\res\\outImage2.jpg"));


    }

}
