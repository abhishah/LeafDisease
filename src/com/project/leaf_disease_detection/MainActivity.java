package com.project.leaf_disease_detection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity implements CvCameraViewListener2 {

	private static final String TAG = "OCVSample::Activity";

	private MyCameraView mOpenCvCameraView;
	private List<android.hardware.Camera.Size> mResolutionList;
	private Mat mRgba;
	private Mat mGray;
	private static Mat centersofcluster;
	private static Mat labels;
	TermCriteria criteria;
	int height = 0, width = 0;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
				Log.w(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
				break;
			default:
				super.onManagerConnected(status);
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.w(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_main);
		mOpenCvCameraView = (MyCameraView) findViewById(R.id.java_surface_view);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);

		// mResolutionList = mOpenCvCameraView.getResolutionList();
		// android.hardware.Camera.Size resolution = mResolutionList.get(4);
		// height = resolution.height;
		// width = resolution.width;
		// Log.w("resolution:height", height + ", width : " + width + "");
		// mOpenCvCameraView.setResolution(resolution);
		// resolution = mOpenCvCameraView.getResolution();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this,
				mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		mGray = new Mat();
		mRgba = new Mat();
		criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER,
				100, 0.1);
	}

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { mResolutionList
	 * = mOpenCvCameraView.getResolutionList(); android.hardware.Camera.Size
	 * resolution = mResolutionList.get(4); height = resolution.height; width =
	 * resolution.width; Log.w("resolution:height", height + ", width : " +
	 * width + ""); mOpenCvCameraView.setResolution(resolution); resolution =
	 * mOpenCvCameraView.getResolution(); return true; }
	 */
	@Override
	public void onCameraViewStopped() {
		mGray.release();
		mRgba.release();
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		
		// InputArray data; int K; InputOutputArray bestLabels; TermCriteria
		// criteria; int attempts; int flags; OutputArray centers=noArray();

		// Core.flip(mRgba, mRgba, 1);
		// Core.flip(mGray, mGray, 1);
		// if (mAbsoluteEyeSize == 0) {
		// int height = mGray.rows();
		// if (Math.round(height * mRelativeEyeSize) > 0) {
		// mAbsoluteEyeSize = Math.round(height * mRelativeEyeSize);
		// }
		// }
		// mGray.copyTo(prevImg); // Mat prevImg

		
		// mHSV = mGray;
		
		// List<Mat> hsv_planes = new ArrayList<Mat>(3);
		// Core.split(mHSV, hsv_planes);
		//
		//
		// Mat channel = hsv_planes.get(2);
		// channel = Mat.zeros(mHSV.rows(),mHSV.cols(),CvType.CV_8UC1);
		// hsv_planes.set(2,channel);
		// Core.merge(hsv_planes,mHSV);
		// Mat clusteredHSV = new Mat();
		//
		mRgba = inputFrame.rgba();
		mGray = inputFrame.gray();
		Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGBA2RGB);
		Imgproc.cvtColor(mGray, mGray, Imgproc.COLOR_RGB2Lab);
		Imgproc.medianBlur(mGray, mGray, 1);
       // Imgproc.cvtColor(mGray, mGray, Imgproc.COLOR_Lab2LRGB);
        int pixel_index=0;
        Mat singlechannel=Mat.zeros(mGray.cols()*mGray.rows(), 5,CvType.CV_32F);
        for(int i=0;i<mGray.rows();i++){
        	for(int j=0;j<mGray.cols();j++){
        	singlechannel.put(pixel_index, 0, (float)i/ mGray.rows());
        	singlechannel.put(pixel_index, 1, (float)j / mGray.cols());
        	double data[]=mGray.get(i, j);
            singlechannel.put(pixel_index, 2, (float)data[0]/100.0)	;
           	singlechannel.put(pixel_index, 3, (float)(data[1]+127)/255.0)	;
           	singlechannel.put(pixel_index, 4, (float)(data[2]+127)/255.0)	;
			Log.v("pixel r value", " "+data[0]+" "+data[1]+" "+data[2]);
        	pixel_index++;
        	}
        }
        int k=3;
        TermCriteria criteria=new TermCriteria(TermCriteria.MAX_ITER+TermCriteria.EPS,10,1.0);
		Mat centers = new Mat();
		labels=new Mat();
		Core.kmeans(singlechannel, k, labels, criteria, 10,
				Core.KMEANS_RANDOM_CENTERS, centers);
		int color[]=new int[k];
		String elements="";
		for(int i=0;i<centers.rows();i++){
			for (int j=0;j<centers.cols();j++){
				double center1[]=centers.get(i, j);
				
				for(int p=0;p<center1.length;p++){
					elements+=center1[p]+" ";
				}
			}
		}
		Mat sampledimage1=new Mat(mGray.rows(),mGray.cols(),mGray.type());
		Mat sampledimage2=new Mat(mGray.rows(),mGray.cols(),mGray.type());
		Mat sampledimage3=new Mat(mGray.rows(),mGray.cols(),mGray.type());
		for(int y=0;y<mGray.rows();y++	){
			for(int x=0;x<mGray.cols();x++){
				int index=y*mGray.rows()+x;
				int[] cluster_index=new int[1];
				labels.get(y, x,cluster_index);
				
				Log.v("Cluster index", " "+cluster_index[0]+" "+(int)cluster_index[0]);
				if((int)cluster_index[0]==0){
					Log.v("Cluster index", " "+sampledimage1);
					sampledimage1.put(y, x, mGray.get(y, x));
				} else if((int)cluster_index[0]==1){
					Log.v("Cluster index", " "+sampledimage2);
					sampledimage2.put(y, x, mGray.get(y, x));
				} else{Log.v("Cluster index", " "+sampledimage3);
					sampledimage3.put(y, x, mGray.get(y, x));
				}
				
			}
		}
		Log.v("elements in center1", centers.size()+"  "+elements);
		
		Log.i("Centers:", " - ");
		Log.e("Points count:", "sent return");
		return sampledimage1;
		
		/*Vector<Mat> threeChannels=new Vector<Mat>(3);
		Core.split(mGray, threeChannels);
		Mat Lch=threeChannels.get(0);
		Mat ach=threeChannels.get(1);
		Mat bch=threeChannels.get(2);
		List<Mat> abch=new ArrayList<Mat>();
		abch.add(0,ach);
		abch.add(1,bch);
		List<Mat> sampled_set=new ArrayList<Mat>();
		sampled_set.add(0,new Mat(abch., type));
		Mat labels=new Mat();
		TermCriteria tc=new TermCriteria(TermCriteria.MAX_ITER+TermCriteria.EPS,10,1.0);
		Mat center=new Mat();
		int ch[]={0,0,1,1};
		MatOfInt from_to=new MatOfInt(ch);
		try{Core.mixChannels(abch, sampled_set,from_to);
		TermCriteria tc=new TermCriteria(TermCriteria.MAX_ITER+TermCriteria.EPS,10,1.0);
		Mat center=new Mat();
		Mat label=new Mat();
		Core.kmeans(mGray, 3, label, tc,10, Core.KMEANS_RANDOM_CENTERS, center);*/		
		/*mGray.convertTo(mHSV, CvType.CV_32FC3);
	
		Imgproc.cvtColor(mGray, mGray, Imgproc.COLOR_HSV2RGB);
		Mat clusters = cluster(mGray, 3).get(0);
		Imgproc.cvtColor(clusters, clusters, Imgproc.COLOR_RGB2Lab);
		MatOfInt label0 = new MatOfInt();
		Mat mask = new Mat();
		MatOfInt label1 = new MatOfInt();
		MatOfInt label2 = new MatOfInt();
		MatOfInt mHistSize = new MatOfInt(25);
		MatOfFloat mRanges = new MatOfFloat(0f, 256f);
		Mat sample1 = centersofcluster.row(0);
		Mat sample2 = centersofcluster.row(1);
		Mat sample3 = centersofcluster.row(2);
		Mat hist1 = new Mat();
		Mat hist2 = new Mat();
		Mat hist3 = new Mat();
		Imgproc.calcHist(Arrays.asList(sample1), label0, mask, hist1,
				mHistSize, mRanges);
		Imgproc.calcHist(Arrays.asList(sample2), label1, mask, hist2,
				mHistSize, mRanges);
		Imgproc.calcHist(Arrays.asList(sample3), label2, mask, hist3,
				mHistSize, mRanges);
		List hist1list = Arrays.asList(hist1);
		Double max1 = Collections.max(hist1list);
		List hist2list = Arrays.asList(hist2);
		Double max2 = Collections.max(hist2list);
		List hist3list = Arrays.asList(hist3);
		Double max3 = Collections.max(hist3list);
		Mat firstimage = new Mat();
		Mat secondimage = new Mat();
		// Mat thirdimage = new Mat();
		Mat leafdiseased = new Mat();
		if (max1 == Math.max(max1, Math.max(max2, max3))) {
			firstimage = sample1;
			secondimage = (max2 > max3) ? sample2 : sample3;
			if (max2 > max3)
				Core.max(hist1, hist2, leafdiseased);
			else
				Core.max(hist1, hist3, leafdiseased);
		} else if (max2 == Math.max(max1, Math.max(max2, max3))) {
			firstimage = sample2;
			secondimage = (max1 > max3) ? sample1 : sample3;
			if (max1 > max3)
				Core.max(hist2, hist1, leafdiseased);
			else
				Core.max(hist2, hist3, leafdiseased);
		} else {
			firstimage = sample3;
			secondimage = (max2 > max1) ? sample2 : sample1;
			if (max2 > max1)
				Core.max(hist3, hist2, leafdiseased);
			else
				Core.max(hist3, hist1, leafdiseased);
		}

		/*
		 * Mat centers = new Mat(); Core.kmeans(mHSV, 3, clusteredHSV, criteria,
		 * 10, Core.KMEANS_PP_CENTERS, centers);
		 * 
		 * Log.e("Centers size:", centers.size() + ""+mGray.size());
		 * Log.e("Centers rows:", centers.rows() +
		 * ""+centers.toString()+" "+mGray.toString()); Log.e("Centers cols:",
		 * centers.cols() + ""); double elements[]=centers.get(0, 0); String
		 * element=null; for(int i=0;i<elements.length;i++){
		 * element+=elements[i]+"   "; } Log.v("elements",
		 * element+" "+elements.length); //SelectionofCluster(clusteredHSV);
		 * String printThisShit = ""; int ctr = 0; for (int i = 0; i <
		 * centers.rows(); i++) { // for (int j = 0; j < centers.cols(); j++) {
		 * // printThisShit += "\t(" + i + ", " + j + ") = " // + centers.get(i,
		 * j)[0];
		 * 
		 * // if (centers.get(i, j).length > 1) // ctr++; // }
		 * 
		 * 
		 * 
		 * Point p = new Point((centers.get(i, 0)[0]), (centers.get(i, 1)[0]));
		 * Core.circle(mGray, p, 2, new Scalar(0, 255, 0)); }
		 * 
		 * MatOfInt mChannels = new MatOfInt(0);// (0) Mat mMat0 = new Mat();
		 * Mat hist = new Mat(); MatOfInt mHistSize = new MatOfInt(25);
		 * MatOfFloat mRanges = new MatOfFloat(0f, 256f);
		 * Imgproc.calcHist(Arrays.asList(centers), mChannels, mMat0, hist,
		 * mHistSize, mRanges); Log.w("Points:", printThisShit+"hist");
		 * Log.e("Points count:", ctr + "");
		 */
		
		
	}

	/*
	 * private void SelectionofCluster(Mat clusteredHSV) { // TODO
	 * Auto-generated method stub MatOfInt mChannels1 = new MatOfInt(0); Mat
	 * hist1=new Mat(); MatOfInt mHistSize = new MatOfInt(25); MatOfFloat
	 * mRanges = new MatOfFloat(0f, 256f); Mat mMat0 = new Mat();
	 * Imgproc.calcHist(Arrays.asList(clusteredHSV.row(0)), mChannels1,mMat0 ,
	 * hist1,mHistSize, mRanges); List mchannel=hist1.toList();
	 * Collections.sort(mchannel); Float max_channel1=(Float)
	 * mchannel.get(mchannel.size()-1); MatOfInt mChannels2 = new MatOfInt(0);
	 * Mat hist2=new Mat(); Mat mMat1 = new Mat();
	 * Imgproc.calcHist(Arrays.asList(clusteredHSV.row(0)), mChannels1,mMat1 ,
	 * hist2,mHistSize, mRanges); List mchannel=hist2.;
	 * Collections.sort(mchannel); Float max_channel1=(Float)
	 * mchannel.get(mchannel.size()-1);MatOfInt mChannels1 = new MatOfInt(0);
	 * Mat hist1=new Mat(); MatOfInt mHistSize = new MatOfInt(25); MatOfFloat
	 * mRanges = new MatOfFloat(0f, 256f); Mat mMat0 = new Mat();
	 * Imgproc.calcHist(Arrays.asList(clusteredHSV.row(0)), mChannels1,mMat0 ,
	 * hist1,mHistSize, mRanges); List mchannel=mChannels1.toList();
	 * Collections.sort(mchannel); Float max_channel1=(Float)
	 * mchannel.get(mchannel.size()-1);
	 * 
	 * }
	 */
	public static List<Mat> cluster(Mat cutout, int k) {
		Mat samples = cutout.reshape(1, cutout.cols() * cutout.rows());
		Mat samples32f = new Mat();
		samples.convertTo(samples32f, CvType.CV_32F, 1.0 / 255.0);

		TermCriteria criteria=new TermCriteria(TermCriteria.MAX_ITER+TermCriteria.EPS,10,1.0);
		Mat centers = new Mat();
		labels=new Mat();
		Core.kmeans(samples32f, k, labels, criteria, 10,
				Core.KMEANS_PP_CENTERS, centers);
		centersofcluster = centers.clone();
		samples.release();
		samples32f.release();
		return showClusters(cutout, labels, centers);
	}

	private static List<Mat> showClusters(Mat cutout, Mat labels, Mat centers) {
		centers.convertTo(centers, CvType.CV_8UC1, 255.0);
		centers.reshape(3);

		List<Mat> clusters = new ArrayList<Mat>();
		for (int i = 0; i < centers.rows(); i++) {
			clusters.add(Mat.zeros(cutout.size(), cutout.type()));
		}

		Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
		for (int i = 0; i < centers.rows(); i++)
			counts.put(i, 0);

		int rows = 0;
		for (int y = 0; y < cutout.rows(); y++) {
			for (int x = 0; x < cutout.cols(); x++) {
				int label = (int) labels.get(rows, 0)[0];
				int r = (int) centers.get(label, 2)[0];
				int g = (int) centers.get(label, 1)[0];
				int b = (int) centers.get(label, 0)[0];
				counts.put(label, counts.get(label) + 1);
				clusters.get(label).put(y, x, b, g, r);
				rows++;
			}
		}
		System.out.println(counts);
		return clusters;
	}
}