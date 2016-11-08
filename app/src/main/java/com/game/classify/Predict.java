package com.game.classify;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import android.content.Context;

import com.game.dataStruct.Point;

public class Predict {
	private Context context;
	private double ratio[];                       //P(y)概率
	private double XYRatio[][]=new double[10][32*32];  //P(X|Y)概率 

	public Predict(Context context){               //构造过程中加载分类模型数据
		this.context=context;
		loadData();
	}

	/*
	 * 读取模型
	 */
	private void loadData(){
		InputStream input;
		try {
			input = context.getAssets().open("ratio.txt");
			BufferedReader read = new BufferedReader(new InputStreamReader(input));
			String line = "";
			ratio=new double[10];
			for(int i=0;i<10;++i){
				line=read.readLine();
				ratio[i]=Double.parseDouble(line);
			}
			read.close();

			input=context.getAssets().open("XYRatio.txt");
			BufferedReader read2 = new BufferedReader(new InputStreamReader(input));

			int k=0;
			while((line=read2.readLine())!=null){
				String[] str=line.split("/");
				for(int i=0;i<str.length;++i){
					XYRatio[i][k]=Double.parseDouble(str[i]);
				}
				++k;
			}
			read2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 对数组列表点集分类
	 * @param list
	 * @return int
	 */
	public int predictList(ArrayList<Point> list){
		int []predictData=GetClassMatrix.getClassifyMatrix(list);
		return predict(predictData);
	}

	/**
	 * 对一维数组格式的矩阵数据进行分类
	 * @param int[]
	 * @return int
	 */
	private int predict(int [] testData){
		double predictRatio[]=new double[10];
		for(int i=0;i<32*32;++i)
			for(int j=0;j<10;++j){
				if(testData[i]!=0)
					predictRatio[j]-=Math.log(XYRatio[j][i]);
				else
					predictRatio[j]-=Math.log(1-XYRatio[j][i]);
			}
		int min=0;
		for(int i=1;i<10;++i)
			if((predictRatio[i]-Math.log(ratio[i]))<(predictRatio[min]-Math.log(ratio[i])))
				min=i;
		return min;
	}
}
