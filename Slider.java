package SliderGame;

/**
 * Description: Practice program for Slider Game
 * Date: November 29th, 2018
 * Original Author: Mr. Roller
 * Modifications: Jenny Tai
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class Slider extends JFrame implements ActionListener {
    private final int lines; //The size of the game
    private JFrame myFrame; 
    private JPanel sliderPanel,functionPanel;
    private JLayeredPane layerPane; //To put together two panels
    private JButton[][] buttons; //The slider buttons
    private JButton[] movable; //For stupid AI to choose from
    private JButton scramble,steps,intro,suggest,stupidAI;
    //Buttons in functionPanel
    private int R,C,step,distance,lastHint; //R&C record whereabout of empty pieces
    //Distance evaluates situation and lastHint(text on button) avoid repeating AI move 
    boolean keepHinting; //For hint function to only appear once

    public static void main(String[] args) {
        new Slider();      //Run constructor for class
    }
        
    public Slider(){
        //Set up game size
        Object[] difficuties = {"Dumb","Easy", "Normal","Hard","Very Hard","Hell" };
        int level = JOptionPane.showOptionDialog(null,"Choose the SliderGame Difficulty", "Set Level",
                JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE, null,
                difficuties, difficuties[2]) +3;
        if (level ==2)
            level = 5;
        lines = level;
        
        //JFrame, JLayerPane and 2 panels
        myFrame = new JFrame("Slider Practice GUI");
        myFrame.setSize(800,600);   
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myFrame.setBackground(Color.black);
        myFrame.setResizable(false);
        myFrame.setLocationRelativeTo(this);
        
        sliderPanel = new JPanel();
        sliderPanel.setBounds(5, 5, 784, 475);
        sliderPanel.setLayout(new GridLayout(lines,lines,4,4)); 									   // vert/horz dividers
        sliderPanel.setBackground(Color.black); 
        
        functionPanel= new JPanel();
        functionPanel.setBounds(0, 485, 795, 86);
        functionPanel.setLayout(new GridLayout(1,3,2,0));
        functionPanel.setBackground(Color.black);

        layerPane = new JLayeredPane();
        layerPane.add(functionPanel,1);
        layerPane.add(sliderPanel,2);
        
        //Set up buttons
        buttons = new JButton[lines][lines];
        for (int i=0; i<lines;i++){ //i row
            for (int j=0;j<lines;j++){ // j column
                buttons[i][j] = new JButton(""+(i*lines+j+1));
                buttons[i][j].setBackground(Color.white);
                buttons[i][j].setForeground(Color.blue);   //Text colour
                buttons[i][j].setFont(new Font("Arial", Font.BOLD, 26));
                buttons[i][j].addActionListener(this);   //Set up ActionListener on each button
                sliderPanel.add(buttons[i][j]);
                if ((i%2==0&&j%2==0)||(i%2!=0&&j%2!=0))
                    buttons[i][j].setBackground(Color.orange);
            }
        }
        
        //Make and record empty slider piece
        buttons[lines-1][lines-1].setVisible(false);
        R=lines-1;
        C=lines-1;
        
        //Initialize the game
        instruction();
        scramble();
        step = 0;
        lastHint=0;
        
        //Set up function buttons
        scramble = new JButton("Restart");
        steps = new JButton("Steps: "+step);
        intro = new JButton("Rules");
        suggest = new JButton("Hint");
        stupidAI = new JButton("AI Move");
        
        Font f = new Font("Papyrus",Font.BOLD,20);
        scramble.setFont(f);
        steps.setFont(f);
        intro.setFont(f);
        suggest.setFont(f);
        stupidAI.setFont(f);
        
        scramble.addActionListener(this);
        intro.addActionListener(this);
        suggest.addActionListener(this);
        stupidAI.addActionListener(this);
        
        functionPanel.add(scramble);
        functionPanel.add(steps);
        functionPanel.add(intro);
        functionPanel.add(suggest);
        functionPanel.add(stupidAI);
        
        //Turn on JFrame
        myFrame.setContentPane(layerPane);
        myFrame.setVisible(true); 
    }
	
    @Override
    public void actionPerformed(ActionEvent e) {
        //Present corresponding function when pressed buttons
        if (e.getSource()==scramble){
            scramble();
            step = 0;
            steps.setText("Steps: "+step);
            
        } else if (e.getSource()==intro){
            instruction();
            
        } else if (e.getSource()==suggest){
            getHint();
            
        } else if (e.getSource()==stupidAI){
            JButton suggestion = stupidSuggestion();
            lastHint = Integer.parseInt(suggestion.getText());
            clickButton(suggestion);
            step ++;
            steps.setText("Steps: "+step);
            checkWin();
        
          // When user move a slide
        } else if (e.getSource().getClass() == JButton.class){
            clickButton((JButton)e.getSource());
            step ++;
            steps.setText("Steps: "+step);
            checkWin();
        }
    }
     
    // swap 2 adjcent buttons, renew empty slide
    private void clickButton(JButton b){
        for (int i = 0; i<lines; i++) {
            for (int j = 0;j<lines; j++) {
                if (b.equals(buttons[i][j])){
                    if ((j==C && (i==R-1 || i==R+1))||(i==R && (j==C-1 || j==C+1))){
                        buttons[R][C].setText(buttons[i][j].getText());
                        buttons[R][C].setBackground(buttons[i][j].getBackground());
                        buttons[R][C].setVisible(true);	
                        buttons[i][j].setVisible(false); 
                        R=i;
                        C=j;
                        getMovable();
                    }
                    break;
                }
            }
        }
    }
    
    // end or restart the game if won
    private void checkWin(){
        if (getDis()==0){
            int x = JOptionPane.showConfirmDialog(null,"You win!!!!\nYou used "
                    +step+" steps\nWanna replay?","Slider Game",JOptionPane.CANCEL_OPTION);
            if (x!=0)
                System.exit(0);
            myFrame.setVisible(false);
            new Slider();
        }
    }
    
    
    //mix the slides
    private void scramble() {
        for (int i=0;i<500;i++){
            int index = (int)(Math.random()*getMovable().length);
            JButton x = movable[index];
            clickButton(x);
        }
    }
    
    //show instuction dialog
    private void instruction() {
        String dialog = "The goal is to rearrange this panel in order from 1-"
                + (lines*lines-1)
                + " \nwith a blank space at the bottom right corner. \n"
                + "Like that:";
        for (int i=0;i<lines;i++){
            dialog+="\n";
            for (int j=1+lines*i;j<lines*i+lines+1;j++){
                if (j<10)
                    dialog+=" ";
                dialog += j+"  ";
                if (j<10)
                    dialog+=" ";
            }
        }
        dialog = dialog.substring(0, dialog.length()-4);
        JOptionPane.showMessageDialog(null,dialog);
    }

    // Give hint to user according to current situation
    private void getHint() {
        keepHinting = true;
        for (int correctR =0; correctR<lines-1;correctR++){
            for (int i=1+correctR*lines;i<lines*(correctR+1)-1;i++){
                hints(i, correctR, i%lines-1,true,"Move the blank space to the side of "
                        + i +", then move "+i+" to where it should be");
            }
            hints(lines*(correctR+1)-1, correctR, lines-1,Integer.parseInt(buttons[correctR][lines-2].getText())!=lines*(correctR+1)-1
                    || Integer.parseInt(buttons[correctR][lines-1].getText())!=lines*(correctR+1),
                    "Move "+(lines*(correctR+1)-1)+" to the last column of row " +(correctR+1)
                    +", \nwithout the disturbing correct pieces in that row");

            hints(lines*(correctR+1), correctR, lines-1,true,"Move "+(lines*(correctR+1))+" to just below "+(lines*(correctR+1)-1)
                    +", \nthen with blank space at the left of "+(lines*(correctR+1)-1)
                    +", turn the 2 pieces counter-clockwise into the correct position");

            for (int i=(lines+1)*(correctR+1);i<(lines-2)*lines;i+=lines){
                hints(i, i/lines, correctR,true,"Move the blank space to the side of "
                        + i +", then move "+i+" to where it should be");
            }

            hints((lines-2)*lines+1+correctR, lines-1, correctR,Integer.parseInt(buttons[lines-2][correctR].getText())!=(lines-2)*lines+1+correctR
                    || Integer.parseInt(buttons[lines-1][correctR].getText())!=lines*(lines-1)+1+correctR,
                    "Move "+((lines-2)*lines+1+correctR)+" to the last row of column " +(correctR+1));

            hints(lines*(lines-1)+1+correctR, lines-1, correctR,true,"Move "+(lines*(lines-1)+1+correctR)+" to the right of "
                    +((lines-2)*lines+1+correctR)+", \nthen with blank space at the top of "
                    +(lines*(lines-1)+1+correctR)+", turn the 2 pieces clockwise into the correct position");
        }
        
    }
    // repetitive checking and outputing
    private void hints(int value, int r, int c,boolean condition ,String intro) {
        if (keepHinting){
            for (int j=0; j<lines;j++){
                for (int k=0; k<lines;k++){
                    if (Integer.parseInt(buttons[r][c].getText())!=value 
                            && Integer.parseInt(buttons[j][k].getText())==value
                            && condition
                            ||!buttons[r][c].isVisible() ){
                        JOptionPane.showConfirmDialog(null,intro,"Slider Game",JOptionPane.CANCEL_OPTION);
                        keepHinting=false;
                        break;
                    }
                }
            }
        }
    }
    
    
    // STUPID AI with its recursive method to check 
    private JButton stupidSuggestion() {
        distance = 10000;
        JButton best = buttons[0][0];
        // do it to every possibility in currect situation
        for (JButton moveChoice : getMovable()) {
            // can't goes to a loop
            if(Integer.parseInt(moveChoice.getText())!=lastHint){
                JButton blank = buttons[R][C];
                clickButton(moveChoice);
                // if 1 step away, win
                if (getDis()==0){
                    distance = getDis();
                    best = moveChoice;
                    break;
                } else { // else, goes into the self-refering method to get the best
                    best = getTheBest(best,5+(8-lines)/2,moveChoice,moveChoice);
                }
                clickButton(blank);
            }
        }
        return best;
    }
    
    private JButton getTheBest(JButton best,int limit,JButton firstMove,JButton lastMove){
        // Keep finding the best until reaches the limit or win
        if (limit!=0){
            for (JButton moveChoice : getMovable()) {
                if(Integer.parseInt(moveChoice.getText())!=Integer.parseInt(lastMove.getText())){
                    JButton blank = buttons[R][C];
                    clickButton(moveChoice);
                    if (getDis()==0){
                        distance = getDis();
                        best = firstMove;
                        break;
                    } else {
                        best = getTheBest(best,limit-1,firstMove,moveChoice);
                    }
                    clickButton(blank);
                }
            }
        // record the best
        } else {
            if (getDis()<distance){
                distance = getDis();
                best = firstMove;
            }
        }
        return best;
    }
    
    // calculate how far to win for each slide and add them togather as a score
    private int getDis(){
        int temp = 0;
        for (int i=0; i<lines;i++){ //i row
            for (int j=0;j<lines;j++){
                if (buttons[i][j].isVisible()){
                    int text = Integer.parseInt(buttons[i][j].getText())-1;
                    int deltaR = i - (text/lines);
                    int deltaC = j - (text%lines);
                    if (deltaR<0)
                        deltaR *= -1;
                    if (deltaC<0)
                        deltaC *= -1;
                    int dis = (deltaR+deltaC)*100/(text/lines+1)/(text%lines+1);
                    temp += dis;
                }
            }
        }
        return temp;
    }
    
    //To get movable slide for stupid AI
    private JButton[] getMovable(){
        if ( (R==0 && C==0)||(R==lines-1 && C==0)||(R==0 && C==lines-1)||(R==lines-1 && C==lines-1)){
            movable = new JButton[2];
            if (R==0 && C==0){
                movable[0] = buttons[R+1][C];
                movable[1] = buttons[R][C+1];
            } else if (R==lines-1 && C==0){
                movable[0] = buttons[R-1][C];
                movable[1] = buttons[R][C+1];
            } else if (R==0 && C==lines-1){
                movable[1] = buttons[R][C-1];
                movable[0] = buttons[R+1][C];
            } else if (R==lines-1 && C==lines-1){
                movable[0] = buttons[R-1][C];
                movable[1] = buttons[R][C-1];
            }
        } else if (R==0||R==lines-1||C==0||C==lines-1){
            movable = new JButton[3];
            if (R==0){
                movable[0] = buttons[R+1][C];
                movable[1] = buttons[R][C-1];
                movable[2] = buttons[R][C+1];
            } else if (R==lines-1){
                movable[0] = buttons[R-1][C];
                movable[1] = buttons[R][C-1];
                movable[2] = buttons[R][C+1];
            } else if (C==lines-1){
                movable[0] = buttons[R-1][C];
                movable[1] = buttons[R][C-1];
                movable[2] = buttons[R+1][C];
            } else if (C==0){
                movable[0] = buttons[R-1][C];
                movable[1] = buttons[R+1][C];
                movable[2] = buttons[R][C+1];
            }
        } else {
            movable = new JButton[4];
            movable[0] = buttons[R-1][C];
            movable[1] = buttons[R+1][C];
            movable[2] = buttons[R][C-1];
            movable[3] = buttons[R][C+1];
        }
        
        return movable;
    }
    
}