import java.util.HashMap;
import java.util.Map;
import java.lang.Math;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.io.File;
import java.io.FileWriter;

class Assembler{
    private static Map<String,Integer> MemAddrs;
    static{
        MemAddrs = new HashMap<String,Integer>(){{
            put("SP",0);
            put("LCL",1);
            put("ARG",2);
            put("THIS",3);
            put("THAT",4);
            put("R0",0);
            put("R1",1);
            put("R2",2);
            put("R3",3);
            put("R4",4);
            put("R5",5);
            put("R6",6);
            put("R7",7);
            put("R8",8);
            put("R9",9);
            put("R10",10);
            put("R11",11);
            put("R12",12);
            put("R13",13);
            put("R14",14);
            put("R15",15);
            put("SCREEN",16384);
            put("KBD",24576);  
        }};
    }
    
    private static Map<String,String> compAIs0;
    static{
        compAIs0 = new HashMap<String,String>(){{
            put("0","101010");
            put("1","111111");
            put("-1","111010");
            put("D","001100");
            put("A","110000");
            put("!D","001101");
            put("!A","110001");
            put("-D","001111");
            put("-A","110001");
            put("D+1","011111");
            put("A+1","110111");
            put("D-1","001110");
            put("A-1","110010");
            put("D+A","000010");
            put("D-A","010011");
            put("A-D","000111");
            put("D&A","000000");
            put("D|A","010101");
        }};
    }
    
    private static Map<String,String> compAIs1;
    static{
        compAIs1 = new HashMap<String,String>(){{
            put("M","110000");
            put("!M","110001");
            put("-M","110001");
            put("M+1","110111");
            put("M-1","110010");
            put("D+M","000010");
            put("D-M","010011");
            put("M-D","000111");
            put("D&M","000000");
            put("D|M","010101");
        }};
    }
    
    private static Map<String,String> dest;
    static{
        dest = new HashMap<String,String>(){{
            put("M","001");
            put("D","010");
            put("MD","011");
            put("A","100");
            put("AM","101");
            put("AD","110");
            put("AMD","111");
        }};
    }
    
    private static Map<String,String> jump;
    static{
        jump = new HashMap<String,String>(){{
            put("JGT","001");
            put("JEQ","010");
            put("JGE","011");
            put("JLT","100");
            put("JNE","101");
            put("JLE","110");
            put("JMP","111");
        }};
    }
    private static boolean isInteger(String s){
        boolean valid;
        try{
            Integer.parseInt(s);
            valid = true;
        }
        catch(NumberFormatException ex){
            valid = false;
        }
        return valid;
            
    }
    
    private static String dec2Bin(int i){
        String newS = "";
        for(int j = 15; j >= 0; j--){
            if(i - (int)Math.pow(2,j) >= 0){
                i = i - (int)Math.pow(2,j);
                newS = newS + "1";
            }
            else
                newS = newS + "0";
        }
        return newS;
    }
    
    
    public static void main(String[] args){
        BufferedReader reader;
        try{
            //name of the input file
            String inputFile = args[0];
            //begin reading from the input file
            BufferedReader firstReader = new BufferedReader(new FileReader(inputFile));
            //used to create other file names
            String fileTitle = inputFile.substring(0,inputFile.indexOf('.'));
            //holds the result of the 1st pass
            String tempFile = fileTitle + "temp.asm";
            //holds the final result
            String finalFile = fileTitle + ".hack";
           
            
            //****************1st pass****************
           

            //create output file (for 1st pass)
            File firstPass = new File(tempFile);
            FileWriter firstWriter = new FileWriter(firstPass);
            
            String line = firstReader.readLine();
            int lineNum = 0;
            while(line != null){
                if(!line.isEmpty()){
                    line = line.replaceAll("\\s+","");
                    if(line.contains("//")) { //deals with comments
                        line = line.substring(0,line.indexOf('/'));
                        if(!line.isEmpty()){
                            firstWriter.write(line+"\n");
                            lineNum++;
                        }
                    }
                    else if(line.charAt(0) == '('){ //deals with labels
                        String label = line.substring(1,line.indexOf(')'));
                        MemAddrs.put(label,lineNum);
                    }
                    else{
                        firstWriter.write(line+"\n");
                        lineNum++;
                    }
                }
                line = firstReader.readLine();
            }
            firstReader.close();
            firstWriter.close();
            
            
            //****************2nd pass****************
            BufferedReader secondReader = new BufferedReader(new FileReader(tempFile));
            
            //create output file (for 1st pass)
            File secondPass = new File(finalFile);
            FileWriter secondWriter = new FileWriter(secondPass);
            
            //holds the next possible index for a new variable
            int nextIndex = 16;
            
            line = secondReader.readLine();
            String instruct = ""; //holds each instruction as it's created
            while(line != null){
                if(line.charAt(0) == '@'){ //A-instructions
                    String value = line.substring(1);
                    if(isInteger(value)){
                        instruct = dec2Bin(Integer.parseInt(value));
                    }
                    else if(MemAddrs.containsKey(value)){
                        instruct = dec2Bin(MemAddrs.get(value));
                    }
                    else{
                        MemAddrs.put(value,nextIndex);
                        instruct = dec2Bin(nextIndex);
                        nextIndex++;
                    }
                }
                else{ //C-instructions
                    String destKey = "";
                    if(line.indexOf('=') > -1){ //check for dest
                        destKey = line.substring(0,line.indexOf('='));
                        if(dest.containsKey(destKey))
                            destKey = dest.get(destKey);
                        line = line.substring(line.indexOf('=')+1);
                    }
                    else
                        destKey = "000";
                    
                    String jumpKey = "";
                    if(line.indexOf(';') > -1){ //check for jump
                        jumpKey = line.substring(line.indexOf(';') + 1);
                        if(jump.containsKey(jumpKey))
                            jumpKey = jump.get(jumpKey);
                        line = line.substring(0,line.indexOf(';'));
                    }
                    else
                        jumpKey = "000";
                    
                    String compKey = line;
                    String a = "";
                    if(compAIs0.containsKey(compKey)){
                        a = "0";
                        compKey = compAIs0.get(compKey);
                    }
                    else if(compAIs1.containsKey(compKey)){
                        a = "1";
                        compKey = compAIs1.get(compKey);
                    }
                    else{
                        System.out.println("Comp doesn't exist");
                        throw new IOException("ERROR");
                        
                    }
                    instruct = "111" + a + compKey + destKey + jumpKey;
                }
                secondWriter.write(instruct+"\n");
                line = secondReader.readLine();
            }
            secondReader.close();
            secondWriter.close();
            File deleteFile = new File(tempFile);
            deleteFile.delete();
            
        } catch(IOException e){
            System.err.format("ERROR: Incorrect or Nonexistent File");
        }
    }
}