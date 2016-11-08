package com.game.classify;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;

import com.game.dataStruct.Point;

import java.util.ArrayList;
import java.util.ListIterator;

/*
 * 返回最终的用于测试的矩阵
 */
public class GetClassMatrix {
	/*顶点的坐标,和矩阵的宽度*/
	private static int minX,minY;
	private static int width;

	/**
	 * 由数组点集获取分类数据
	 * @param 点集
	 * @return 用于数字识别的一维数组
	 */
	public static int[] getClassifyMatrix(ArrayList<Point> list){
		int matrix[]=translate(list);
		matrix=resize(matrix, width);

		return matrix;
	}

	/**
	 * 将数组点集转换为一维数组存储的矩阵
	 * @param 屏幕上获取的点集
	 * @return 一维数组存储的矩阵
	 */
	private static int[] translate(ArrayList<Point> list){
		searchMinMax(list);
		int matrix[]=new int[width*width];
		for(int i=0;i<width;++i)                 //初始化数组
			for(int j=0;j<width;++j)
				matrix[i*width+j]=Color.WHITE;
		ListIterator<Point> iterator=list.listIterator();
		Point p=null;
		while(iterator.hasNext()){
			p=iterator.next();
			for(int i=-25;i<=25;++i)
				for(int j=-25;j<=25;++j)
					if( (p.getY()+i)>=minY && (p.getY()+i)<minY+width && (p.getX()+j)>=minX &&(p.getX()+j)<minX+width)
						matrix[(p.getY()+i-minY)*width+p.getX()+j-minX]=Color.BLACK;
		}
		return matrix;
	}

	/**
	 * 查询顶点坐标，和宽度。
	 * @param  屏幕获取的点集
	 * @return void
	 */
	private static void searchMinMax(ArrayList<Point> list){
		ListIterator<Point> iterator=list.listIterator();
		int tempMinX=800,tempMinY=800,tempMaxX=0,tempMaxY=0;
		Point p=null;
		while(iterator.hasNext()){
			p=iterator.next();
			if(p.getX()<tempMinX)
				tempMinX=p.getX();
			if(p.getX()>tempMaxX)
				tempMaxX=p.getX();
			if(p.getY()<tempMinY)
				tempMinY=p.getY();
			if(p.getY()>tempMaxY)
				tempMaxY=p.getY();
		}
		if(tempMaxX-tempMinX>=tempMaxY-tempMinY)
			width=tempMaxX-tempMinX+1;
		else
			width=tempMaxY-tempMinY+1;

		minX=(tempMaxX+tempMinX)/2-width/2;
		minY=tempMinY;
	}

	/**
	 * 将目标图片收缩变化为32*32的一维矩阵
	 * @param   图像数组
	 * @param   图片宽度
	 * @return 调整大小后的以为数组
	 */
	private static int[] resize(int  []grayImage,int width){
		Bitmap bm = Bitmap.createBitmap(width, width,Bitmap.Config.RGB_565);
		bm.setPixels(grayImage, 0, width, 0, 0, width, width);
		Matrix matrix = new Matrix();
		matrix.preScale( (float)32/width,(float)32/width );
		Bitmap mScaleBitmap = Bitmap.createBitmap(bm, 0, 0, width, width, matrix, true);
		int[] nImageData = new int[32*32];//保存所有的像素的数组，图片宽×高
		mScaleBitmap.getPixels(nImageData,0,32,0,0,mScaleBitmap.getWidth(),mScaleBitmap.getHeight());

		for(int i=0;i<32;++i){
			for(int j=0;j<32;++j)
				if(nImageData[i*32+j]==Color.BLACK)
					nImageData[i*32+j]=1;
				else
					nImageData[i*32+j]=0;
		}
		String str=null;
		for(int i=0;i<32;++i){
			str="";
			for(int j=0;j<32;++j)
				str+=nImageData[i*32+j];
			Log.i("data------>",str);
		}
		return nImageData;
	}
}
