package lex;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class LexicalAnalyzer {
	
	/*
	 * 自己实现的扫描和缓存类
	 */
	private Scan scan;
	
	/**
	 * 词法分析器类的构造方法 
	 * @param filename 文件位置和文件名
	 */
	public LexicalAnalyzer(String filename){
		this.scan = new Scan(filename);
	}
	
	public static String outputPath = "Output/";
	/**
	 * 将词法分析结果输出到文件中
	 * @param list 词法分析结果的TokenList
	 * @param filename 需要输出到的文件路径和文件名
	 */
	@SuppressWarnings("resource")
	public void output(ArrayList<Token> list,String filename){
		filename = LexicalAnalyzer.outputPath+filename;
		File file = new File(filename);
		while(file.exists()){
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(
					new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		for(int i = 0;i < list.size();i++){
			try {
				out.writeChar((int)'<');
				out.writeInt(list.get(i).type);
				out.writeChar((int)',');
				out.writeChars(list.get(i).value);
				out.writeChar((int)'>');
				out.writeChar((int)'\n');
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 通过语法分析获得Token序列
	 * @return Token序列
	 */
	public ArrayList<Token> getTokenList(){
		ArrayList<Token> result = new ArrayList<Token>();
		int index = 0;
		while(index < scan.getLength()){
			Token token = analyze(index);
			result.add(token);
			index = scan.getIndex();
		}
		this.scan.retract(scan.getLength()-1);
		return result;
	}
	
	/*
	 * 关键字
	 */
	private String[] keyword ={
		"auto","double","int","struct","break","else","long","switch",
		"case","enum","register","typedef","char","return","union","const",
		"extern","float","short","unsigned","continue","for","signed","void",
		"default","goto","sizeof","volatile","do","if","static","while"
	};

	/**
	 * 对某一位置进行词法分析
	 * @param index 字母开始的位置
	 * @return 单个Token
	 */
	public Token analyze(int index){
		int length = scan.getLength();
		int type = -1;
		String value = "";
		while(index < length){
			char ch = scan.getNextChar();
			index++;
			char ch1 = '\0';
			if(isDigit(ch)){//判断是否为一个数字
				if(Type.isCalc(type)){
					scan.retract(1);
					break;
				}
				if(value == ""){
					value = new Character(ch).toString();
					type = Type.NUM;
				} else {
					value += new Character(ch).toString();
				}
				
			} else if (isLetter(ch)){
				if(Type.isCalc(type)){
					scan.retract(1);
					break;
				}
				String str = scan.getSubStr(index-1, 9);
				String val = null;
				for(int i = 0;i < keyword.length;i++){
					if(str.startsWith(keyword[i])){
						val = keyword[i];
						type = i;
						break;
					}
				}
				if(val == null){
					type = Type.ID;
					if(value == ""){
						value = new Character(ch).toString();
					} else {
						value += new Character(ch).toString();
					}
				} else {
					value = val;
				}
				
			} else {
				if(type == Type.NUM || type == Type.ID){
					scan.retract(1);
					return new Token(type,value);
				}
				switch(ch){
				case '='://==,=
					if(type == -1){
						type = Type.ASSIGN;
						value = "=";
					} else if(type == Type.LT){//<=
						type = Type.LE;
						value = "<=";
					} else if(type == Type.GT){//>=
						type = Type.GE;
						value = ">=";
					} else if(type == Type.ASSIGN){//==
						type = Type.EQUAL;
						value = "==";
					} else if(type == Type.NOT){//!=
						type = Type.NE;
						value = "!=";
					} else if(type == Type.ADD){//+=
						type = Type.INCREASEBY;
						value = "+=";
					} else if(type == Type.SUB){//-=
						type = Type.DECREASEBY;
						value = "-=";
					} else if(type == Type.DIV){///=
						type = Type.DIVBY;
						value = "/=";
					} else if(type == Type.MUL){//*=
						type = Type.MULBY;
						value = "*=";
					}
					break;
				case '+':
					if(type == -1){
						type = Type.ADD;
						value = "+";
					} else if(type == Type.ADD){//++
						type = Type.INCREASE;
						value = "++";
					} 
					break;
				case '-':
					if(type == -1){
						type = Type.SUB;
						value = "-";
					} else if(type == Type.SUB){//--
						type = Type.DECREASEBY;
						value = "--";
					}
					break;
				case '*':
					if(type == -1){
						type = Type.MUL;
						value = "*";
					}
					break;
				case '/':
					if(type == -1){
						type = Type.DIV;
						value = "/";
					}
					break;
				case '<':
					if(type == -1){
						type = Type.LT;
						value = "<";
					}
					break;
				case '>':
					if(type == -1){
						type = Type.GT;
						value = ">";
					}
					break;
				case '!':
					if(type == -1){
						type = Type.NOT;
						value = "!";
					}
					break;
				case '|':
					if(type == -1){
						type = Type.OR_1;
						value = "|";
					} else if(type == Type.OR_1){
						type = Type.OR_2;
						value = "||";
					}
					break;
				case '&':
					if(type == -1){
						type = Type.AND_1;
						value = "&";
					} else if(type == Type.AND_1){
						type = Type.AND_2;
						value = "&&";
					}
					break;
				case ';':
					if(type == -1){
						type = Type.SEMICOLON;
						value = ";";
					}
					break;
				case '{':
					if(type == -1){
						type = Type.BRACE_L;
						value = "{";
					}
					break;
				case '}':
					if(type == -1){
						type = Type.BRACE_R;
						value = "}";
					}
					break;
				case '[':
					if(type == -1){
						type = Type.BRACKET_L;
						value = "[";
					}
					break;
				case ']':
					if(type == -1){
						type = Type.BRACKET_R;
						value = "]";
					}
					break;
				case '(':
					if(type == -1){
						type = Type.PARENTHESIS_L;
						value = "(";
					}
					break;
				case ')':
					if(type == -1){
						type = Type.PARENTHESIS_R;
						value = ")";
					}
					break;
				case '#':
					if(type == -1){
						type = Type.POUND;
						value = "#";
					}
					break;
				case ',':
					if(type == -1){
						type = Type.COMMA;
						value = ",";
					}
					break;
				default:
					break;
				}
				if(!Type.isCalc(type)){
					break;
				}
			}
		}
		Token token = new Token(type,value);
		return token;
	}
	
	private boolean isDigit(char c){
		if((c<='9'&&c>='0')||c=='.'){
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isLetter(char c){
		if((c>='a'&&c<='z')||c=='_'||(c>='A'&&c<='Z')){
			return true;
		} else {
			return false;
		}
	}
	
}
