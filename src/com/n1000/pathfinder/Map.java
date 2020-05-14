package com.n1000.pathfinder;


import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Random;

public class Map  extends MouseAdapter {
    private int pathFinderDimensions = 600;

    private int boxWidth;
    private int boxHeight;

    //Settings
    private int tool = 4; // 0 none, 1 startPoint, 2 FinishPoints, 3 Obstacle.
    private int density = 20; // number of boxes/grid
    private int newSetting = 0; // used to update density
    private int randomBlocks = 20; // random obstacle generator e.g. 20%.

    private int mx = 0; // mouse input x
    private int my = 0; // mouse input y

    private boolean isStartSet;
    private boolean isFinishSet;
    private boolean started; // state
    private boolean isGHF_ParentSwitch = false; // switches between G,H,F cost and parent coordinates ( works only for density 20)
    private boolean isShowingNumbers = false; // Shows information about tiles ( works only for density 20)
    private boolean error = false; //if error present display message
    private boolean isNewSetting = false; //lets render know to update before new loop.
    private boolean mousePressed = false; // for painting

    private Random r = new Random(); // Used for random blocks

    public int startX;
    public int startY;
    public int finishX;
    public int finishY;

    private int speedGearBox = 3; // speed of movements
    private float[] speeds =  {0.5f, 1,2,3,4,5,6,10,12,15,20,30, 60, 120}; // 4 updates per second
    public float speed = speeds[speedGearBox]; // e.g. [3] = 60/0.5 = 120 frames per second

    public boolean finishReached = false;
    public boolean isManualControl = false; // manual way of controlling where algorithm goes.
    public boolean isFourDirections = false; // switches between 4 and 8 directions
    public boolean isAStar = false; //true use A* algorithm else use Dijkstra.

    public int [][][] grid = new int[density][density][6];
    // 0 boxes for drawing map;
    // 1 start distance (G cost)
    // 2 finish distance (H cost)
    // 3 total distance (F cost)
    // 4 parent distance x
    // 5 parent distance y

    public boolean [][][] tracking = new boolean[density][density][2];
    // 0 moves done (tracks previous moves)
    // 1 fastest route (tracks fastest route back)

    private Node node;

    public Map() {
        clear(); //resets everything
    }


    @Override
    public void mouseDragged(MouseEvent e) {
        //Paints the blocks
        if (mousePressed && mx < 600 && started == false && finishReached == false) {

            int nx = e.getX()/boxWidth;
            int ny = e.getY()/boxHeight;

            if (nx >= 0 && nx < density && ny >= 0 && ny < density) {
                if (grid[nx][ny][0] == 0) {
                    if (tool == 3) grid[nx][ny][0] = tool;
                } else if (grid[nx][ny][0] == 3) {
                    if (tool == 0) grid[nx][ny][0] = tool;
                }
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
        //container mouse grid
        mx = e.getX();
        my = e.getY();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int wr = e.getWheelRotation();

        //Random blocks generator
        if (mx > 610 && mx < 790) {
            if (my > 120 && my < 150) {
                randomBlocks += -wr;

                if (randomBlocks < 1) randomBlocks = 1;
                else if (randomBlocks > 80) randomBlocks = 80;
            }
        }

        //Animation speed
        if (mx > 610 && mx < 790) {
            if (my > 350 && my < 390) {
                updateSpeed(wr);
            }
        }

        //Tiles size
        if (mx > 610 && mx < 790 && started == false && finishReached == false) {
            if (my > 400 && my < 440) {
                newSetting = -wr;
                isNewSetting = true;
            }
        }

    }

    public void mousePressed(MouseEvent e) {
        //local variable;
        int mx = e.getX();
        int my = e.getY();

       int x = mx/boxWidth;
       int y = my/boxHeight;
       mousePressed = true;

       if (mx < 600 && started == false && finishReached == false && x < grid.length && y < grid.length ) {
           // check is start/finish has/is being set.
           if (grid[x][y][0] == 1) {
               grid[x][y][0] = 0;
               isStartSet = false;
           } else if (grid[x][y][0] == 2) {
               grid[x][y][0] = 0;
               isFinishSet = false;
           }


           if (tool == 1) {
               //start set
               if (isStartSet == false) {
                   grid[x][y][0] = tool;
                   isStartSet = true;
                   tool = 2;
                   startX = x;
                   startY = y;
               }

           } else if (tool == 2) {
               //finish set
               if (isFinishSet == false) {
                   grid[x][y][0] = tool;
                   isFinishSet = true;
                   tool = 3;
                   finishX = x;
                   finishY = y;
               }
           } else if (tool == 3) {
               //obstacle
               if (grid[x][y][0] == tool) {
                   grid[x][y][0] = 0;
               } else grid[x][y][0] = tool;
           }
       }

       // Manual discovery
        if (mx > 0 && mx < 600 && my > 0 && my < 600) {
            if (isManualControl && node != null) {
                try {
                    node.checkAround(x, y);
                } catch (IndexOutOfBoundsException exception) {
                    error = true;
                }

            }

        }


        if (mx > 610 && mx < 790 ) {

            //switches tools
            if (my > 30 && my < 60) {
                if (mx > 610 && mx < 670) tool = 1;
                else if (mx > 670 && mx < 730) tool = 2;
                else if (mx > 730 && mx < 790) tool = 3;

            } else if (my > 70 && my < 90) {
                //Rubber tool
                tool = 0;
            }

            //reset/clear
            if (my > 190 && my < 220) {
                //clear button
                clear();
            }

            if (started == false && finishReached == false) {

                // Buttons for random block generator
                if (my > 120 && my < 150) {
                    if (mx > 610 && mx < 640) {
                        randomBlocks--;

                    } else if (mx > 760 && mx < 790) {
                        randomBlocks++;
                    }

                    if (randomBlocks < 1) randomBlocks = 1;
                    else if (randomBlocks > 80) randomBlocks = 80;

                } else if (mx > 640 && mx < 760 && my > 155 && my < 175) {
                    //generate button
                    generateRandomObstacle();
                }


                // start algorithms
                if (my > 250 && my < 290) {
                    if (mx > 610 && mx < 700) {
                        //Start algorithm 1 (A*);
                        if (isStartSet && isFinishSet) {
                            started = true;
                            node = new Node(startX, startY, this);
                            isAStar = true;
                        }

                    } else if (mx > 700 && mx < 790) {
                        //Start algorithm 2 (Dijkstra);
                        if (isStartSet && isFinishSet) {
                            started = true;
                            node = new Node(startX, startY, this);
                            isAStar = false;
                        }
                    }

                } else if (my > 400 && my < 440) {
                    // new world size
                    if (mx > 760 && mx < 790) {
                        newSetting = +1;

                    } else if (mx > 610 && mx < 640) {
                        newSetting = -1;
                    }
                    isNewSetting = true;
                }

            }

                if (my > 350 && my < 390) {
                    //Speed buttons
                    if (mx > 760 && mx < 790) {
                        updateSpeed(-1);
                    } else if (mx > 610 && mx < 640) {
                        updateSpeed(1);;
                    }

                }
                //other buttons
                if (my > 450 && my < 490) {
                    if (mx > 610 && mx < 700) {
                        // Numbers off
                        isShowingNumbers = !isShowingNumbers;

                    } else if (mx > 700 && mx < 790) {
                        //Parent/GHF switch
                        if (isShowingNumbers == true) isGHF_ParentSwitch = !isGHF_ParentSwitch;
                    }

                } else if (my > 500 && my < 550) {
                    if (mx > 610 && mx < 700) {
                        isManualControl = !isManualControl;
                    } else if (mx > 700 && mx < 790) {
                        isFourDirections = !isFourDirections;
                    }

                // not developed functionality
                } else if (my > 550 && my < 590) {
                    if (mx > 610 && mx < 700) {
                        //seed set

                    } else if (mx > 700 && mx < 790) {
                        //seed get
                    }
                }


        } // between x610 and x790
    }
    public void mouseReleased(MouseEvent e) {
        mousePressed = false;
    }


    private void clear() {
        //resets/restarts everything to default values
        node = null;
        for (int x = 0; x < density; x++) {
            for (int y = 0; y < density; y++) {
                grid[x][y][0] = 0;

                grid[x][y][1] = 0;
                grid[x][y][2] = 0;
                grid[x][y][3] = 0;

                grid[x][y][4] = -1; // negative grid as not to overlap XY 0 grid.
                grid[x][y][5] = -1; // negative grid as not to overlap XY 0 grid.

                tracking[x][y][0] = false;
                tracking[x][y][1] = false;

                isStartSet = false;
                isFinishSet = false;
                finishReached = false;
                started = false;
                error = false;

                tool = 1;
            }
        }
    }

    private void changeSize() {
        //Changes grid size
        density += newSetting;
        if (density < 1) density = 1;
        grid = new int[density][density][6]; // for drawing map [tool][]
        tracking = new boolean[density][density][2];
        clear();
    }

    private void generateRandomObstacle() {
        //Creates random obstacles
        for (int i = 0; i < grid.length; i++) {
            for (int ii = 0; ii < grid.length; ii++) {

                if (grid[i][ii][0] == 1 || grid[i][ii][0] == 2) continue;
                grid[i][ii][0] = 0;

                int n = r.nextInt(100);
                if (n > 0 && n <= randomBlocks) {
                    grid[i][ii][0] = 3;
                }
            }
        }
    }

    private void updateSpeed(int newGear) {
        //Changes speed
        if (newGear < 0) {
            if (speedGearBox > 0)speedGearBox += newGear;
        }else if (newGear > 0) {
            if (speedGearBox < speeds.length-1) speedGearBox += newGear;
        }
        speed = speeds[speedGearBox];
    }

    public void update() {
        //could probably be somewhere else :P
        boxWidth = (pathFinderDimensions)/density;
        boxHeight = (pathFinderDimensions)/density;

        if (node != null) {
            node.update();
        }
    }

    public void render(Graphics g) {
        //Grid
        for (int h = 0; h < density; h++) { // x
            for (int v = 0; v < density; v++) { // y
                g.setColor(Color.BLACK);
                g.drawRect(boxWidth * h, boxHeight * v, boxWidth, boxHeight);

                if (grid[h][v][0] == 1) {
                    g.setColor(new Color(0, 170, 0));
                    g.fillRect(boxWidth * h + 1, boxHeight * v + 1, boxWidth - 1, boxHeight - 1);
                } else if (grid[h][v][0] == 2) {
                    g.setColor(Color.blue);
                    g.fillRect(boxWidth * h + 1, boxHeight * v + 1, boxWidth - 1, boxHeight - 1);
                } else if (grid[h][v][0] == 3) {
                    g.setColor(Color.black);
                    g.fillRect(boxWidth * h + 1, boxHeight * v + 1, boxWidth - 1, boxHeight - 1);
                }
            }
        }


        //drawRects
        g.setColor(new Color(172,179,208));
        g.fillRect(600,0,200,600);

        g.setColor(Color.black);
        g.drawRect(610, 5, 30, 20); //color display

        g.drawRect(610, 30, 60, 30); //start
        g.drawRect(670, 30, 60, 30); //finish
        g.drawRect(730, 30, 60, 30); //obstacle
        g.drawRect(610, 60, 180, 30); //rubber
        g.drawRect(640, 120, 120, 30); //Randomise walls
        g.drawRect(610, 120, 30, 30); //Randomise walls +
        g.drawRect(760, 120, 30, 30); //Randomise walls -
        g.drawRect(640, 155, 120, 20); //Randomise button
        g.drawRect(610, 190, 180, 30); //reset
        g.drawRect(610, 250, 90, 40); // start 1
        g.drawRect(700, 250, 90, 40); // start 2
        g.drawRect(610, 350, 180, 40); //Speed of animation
        g.drawRect(730, 350, 30, 40); // animation +
        g.drawRect(610, 350, 30, 40); // animation -
        g.drawRect(640, 400, 120, 40); //Size
        g.drawRect(610, 400, 30, 40); // tile size -
        g.drawRect(760, 400, 30, 40); // tiles size +
        g.drawRect(610, 450, 90, 40); // display numbers
        g.drawRect(700, 450, 90, 40); // Distance/parent on off
        g.drawRect(610, 500, 90, 40); // Manual positioning
        g.drawRect(700, 500, 90, 40); // 4/8 way movements
        g.drawRect(610, 550, 90, 40); // seed set
        g.drawRect(700, 550, 90, 40); // seed get


        //Fill
        g.setColor(new Color(0, 170, 0));
        g.fillRect(611, 31, 59, 29); //start tool
        g.setColor(Color.blue);
        g.fillRect(671, 31, 59, 29); //finish tool
        g.setColor(Color.black);
        g.fillRect(731, 31, 59, 29); //obstacle tool
        g.setColor(Color.white);
        g.fillRect(611, 61, 179, 29); // rubber tool

        g.fillRect(641, 121, 119, 29); //Randomise walls
        g.setColor(Color.lightGray);
        g.fillRect(611, 121, 29, 29); //Randomise walls +
        g.fillRect(761, 121, 29, 29); //Randomise walls -
        g.setColor(new Color(0,180,250));
        g.fillRect(641, 156, 119, 19); //Randomise walls button

        g.setColor(new Color(230,4,19));
        g.fillRect(611, 191, 179, 29); //reset
        g.setColor(new Color(100,230, 0));
        g.fillRect(611, 251, 89, 39); // A* start
        g.fillRect(701, 251, 89, 39); // Dijkstra start

        g.setColor(Color.white);
        g.fillRect(641, 351, 119, 39); //Speed of animation
        g.fillRect(641, 401, 119, 39); //Size of tiles
        g.setColor(Color.lightGray);
        g.fillRect(611, 351, 29, 39); //animation +
        g.fillRect(761, 351, 29, 39); //animation -

        g.fillRect(611, 401, 29, 39); //Randomise walls +
        g.fillRect(761, 401, 29, 39); //Randomise walls -

        g.setColor(new Color(167 , 210 , 235));
        g.fillRect(611, 451, 89, 39); // display numbers
        g.fillRect(701, 451, 89, 39); // Distance/parent on off
        g.fillRect(611, 501, 89, 39); // Manual movement
        g.fillRect(701, 501, 89, 39); // 4/8 way movements
        g.fillRect(611, 551, 89, 39); // seed set
        g.fillRect(701, 551, 89, 39); // seed get

        //Text
        Font text = new Font("arial", 1, 12);
        Font large = new Font("arial", 1, 15);
        g.setFont(text);

        g.setColor(Color.black);
        g.drawString("Tools", 680, 20);
        g.setColor(Color.orange);
        g.drawString("Start", 615, 50);
        g.drawString("Finish", 675, 50);
        g.drawString("Obstacle", 735, 50);
        g.setColor(Color.black);
        g.drawString("Rubber", 675, 80);

        g.drawString("Generate blocks (%)", 650, 115);
        g.drawString("" + randomBlocks + " %", 697, 140);
        g.setColor(Color.red);
        g.drawString("<", 620, 140);
        g.drawString(">", 770, 140);
        g.setColor(Color.black);
        g.drawString("Generate", 675, 170);

        g.drawString("Clear / Reset", 670, 210);


        g.drawString("Finder (A*)", 620, 275);
        g.drawString("Searcher", 720, 265);
        g.drawString(" (Dijkstra)", 720, 280);

        if (error) {
            g.setFont(large);
            g.setColor(Color.red);
            g.drawString("Error, reset needed", 620, 325);
        }
        g.setFont(text);
        g.setColor(Color.black);


        g.drawString("Speed", 680, 365);
        g.drawString("Tiles size", 670, 415);

        g.setColor(Color.red);
        g.drawString("" + (120 / speed), 690, 380); //speed
        g.drawString("<", 620, 375); // speed
        g.drawString(">", 770, 375); // speed

        g.drawString("" + density, 690, 430); // tile size
        g.drawString("<", 620, 425); // tile size
        g.drawString(">", 770, 425); // tile size


        g.setColor(Color.black);
        if (isShowingNumbers) {
            g.drawString("Numbers", 630, 465);
            g.setColor(Color.red);
            g.drawString("ON", 650, 480);

            if (isGHF_ParentSwitch) {
                g.drawString("G H F", 720, 475);
            } else {
                g.drawString("Parent x , y", 710, 475);
            }

        } else {
            g.drawString("Numbers", 630, 465);
            g.drawString("OFF", 650, 480);
            g.setColor(Color.gray);
            g.drawString("Numbers OFF", 710, 475);
        }

        g.setFont(text);
        g.setColor(Color.black);
        if (isManualControl) {
            g.drawString("Manual move", 620, 515);
            g.setColor(Color.red);
            g.drawString("ON", 650, 530);
        } else {
            g.drawString("Manual move", 620, 515);
            g.drawString("OFF", 650, 530);
        }

        g.setColor(Color.black);
        if (isFourDirections) {
            g.drawString("4 Directions", 710, 525);
        } else {
            g.drawString("8 Directions", 710, 525);
        }


        if (tool == 1) g.setColor(new Color(0, 170, 0));
        else if (tool == 2) g.setColor(Color.blue);
        else if (tool == 3) g.setColor(Color.black);
        else g.setColor(Color.white);

        g.fillRect(611, 6, 29, 19); // fill toolkit

        //Node distances
        Font font2 = new Font("arial", 0, 15);
        Font font3 = new Font("arial", 0, 20);
        g.setFont(font2);
        g.setColor(Color.black);

        for (int h = 0; h < grid.length; h++) { // x
            for (int v = 0; v < grid.length; v++) { // y

                if (grid[h][v][1] != 0 || grid[h][v][2] != 0 || grid[h][v][3] != 0) {
                    if (!finishReached) {
                        g.setColor(Color.green);
                        g.setFont(font2);
                        g.fillRect(boxWidth * h + 1, boxHeight * v + 1, boxWidth - 2, boxHeight - 2);


                        if (tracking[h][v][0] && !finishReached) {
                            g.setColor(Color.red);
                            g.fillRect(boxWidth * h + 1, boxHeight * v + 1, boxWidth - 1, boxHeight - 1);
                        }
                        if (isShowingNumbers) {
                            if (isGHF_ParentSwitch) {
                                g.setColor(Color.black);
                                g.drawString(grid[h][v][4] + "", boxWidth * (h + 1) - 50, boxHeight * (v + 1) - 40);
                                g.drawString(grid[h][v][5] + "", boxWidth * (h + 1) - 20, boxHeight * (v + 1) - 40);
                            } else {
                                g.setColor(Color.black);
                                g.drawString(grid[h][v][1] + "", boxWidth * (h + 1) - 50, boxHeight * (v + 1) - 40);
                                g.drawString(grid[h][v][2] + "", boxWidth * (h + 1) - 20, boxHeight * (v + 1) - 40);

                                g.setFont(font3);
                                g.drawString(grid[h][v][3] + "", boxWidth * (h + 1) - 35, boxHeight * (v + 1) - 12);
                            }
                        }
                    }
                }
            }
        }

        // If finished show fastest route cords
        if (finishReached) {
            g.setColor(Color.blue);
            for (int x = 0; x < tracking.length; x++) {
                for (int y = 0; y < tracking.length; y++) {
                    if (tracking[x][y][1]) {
                        g.fillRect(boxWidth * x + 1, boxHeight * y + 1, boxWidth - 1, boxHeight - 1);
                    }
                }
            }
        }

        //If grid setting changed
        if (isNewSetting) {
            changeSize();
            newSetting = 0;
            isNewSetting = false;

        }

    }
}






