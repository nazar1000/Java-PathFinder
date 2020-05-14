package com.n1000.pathfinder;

/*
    Possible improvements:
    Add open grids to array and loop through them instead of everything.

 */

public class Node{
    private Map map;

    private int[][][] grid;
    private int timer = 0;

    private int x;
    private int y;


    public Node(int x, int y, Map map) {
        this.map = map;
        this.x = x;
        this.y = y;

        grid = map.grid;

        checkAround(x,y);
    }

    public void update() {
        timer += 1;

        if (timer % map.speed == 0) {
            if (!map.finishReached) {
                checkAround(x, y);
            }
        }
    }


    public void checkAround(int x, int y) {

        for (int i = 0; i < 3; i++) {
            for (int ii = 0; ii < 3; ii++) {

                int posX = i + x - 1;
                int posY = ii + y - 1;

                int cost;

                if (i == 0 && ii == 0 || i == 2 && ii == 0 || i == 0 && ii == 2 || i == 2 && ii == 2 ) {
                    if (map.isFourDirections) {
                        continue; // for 4 way
                    } else cost = 14;


                } else cost = 10;

                //outside zones
                if (posX < 0 || posX >= grid.length || posY < 0 || posY >= grid.length) continue;
                else if (grid[posX][posY][0] == 3 || posX == this.x && posY == this.y) continue;
                else if (this.x < 0 || this.x > grid.length || this.y < 0 || this.y > grid.length) break;

                //distance to finish
                int X_FinishX = Math.abs(posX - map.finishX);
                int Y_FinishY = Math.abs(posY - map.finishY);

                int G_Cost = 0;



                if (grid[posX][posY][1] == 0) G_Cost = grid[x][y][1] + cost;
                else if (grid[posX][posY][1] > grid[x][y][1]+cost) G_Cost = grid[x][y][1]+cost;
                else G_Cost = grid[posX][posY][1];

                int H_Cost = Math.abs(X_FinishX - Y_FinishY) * 10 + (Math.min(X_FinishX, Y_FinishY) * 14);

                grid[posX][posY][1] = G_Cost;
                grid[posX][posY][2] = H_Cost;
                grid[posX][posY][3] = G_Cost + H_Cost;

                map.tracking[x][y][0] = true;


                //Parent node

                // if this is start
                if (x == map.startX && y == map.startY) {
                    grid[posX][posY][4] = map.startX;
                    grid[posX][posY][5] = map.startY;
                    grid[x][y][4] = x;
                    grid[x][y][5] = y;

                // if its neighbour of start or does not have a parent
                } else if (grid[posX][posY][4] == -1) {
                    if (!(posX == map.startX && posY == map.startY)) {
                        grid[posX][posY][4] = x;
                        grid[posX][posY][5] = y;
                    } else {
                        grid[posX][posY][4] = map.startX;
                        grid[posX][posY][5] = map.startY;

                    }
                }
                if (grid[posX][posY][1] > grid[x][y][1]) {
                    if (!(posX == map.startX && posY == map.startY)) {

                        //if current parent of neighbour is further away then change to this as parent
                        if (grid[grid[posX][posY][5]][grid[posX][posY][5]][0] > grid[x][y][1]) {
                            grid[posX][posY][4] = x;
                            grid[posX][posY][5] = y;
                        }
                    }

                } else if (grid[posX][posY][1] < grid[x][y][1]) {
                    //if parent of neighbour is further away from start then change else ignore;
                    if (grid[grid[x][y][5]][grid[x][y][5]][0] > grid[posX][posY][1]) {

                        if (map.tracking[posX][posY][0]) {
                            // if the new neighbour is closer and it has been stepped on.
                            grid[x][y][4] = posX;
                            grid[x][y][5] = posY;
                        }
                    }
                }

                if (x == map.finishX && y == map.finishY && map.isManualControl == true) {
                    map.tracking[this.x][this.y][0] = true;
                    map.finishReached = true;
                    showFastestRoute();
                }
            }
        }
        if (map.isManualControl == false) {
            if (map.isAStar) {
                findNextMove_A();
            } else {
                findNextMove_Dijkstra();
            }
        }
    }


    private void findNextMove_A () {

        int lowestDis = 0;
        int lowestFinDis = 0;
        int lowestX = -1;
        int lowestY = -1;

        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid.length; y++) {

                if (this.x == map.finishX && this.y == map.finishY) {
                    map.tracking[this.x][this.y][0] = true;
                    map.finishReached = true;
                    break;
                    //Finish reached
                    //Start going back
                }

                // If not actual moves
                if (map.tracking[x][y][0] == false && grid[x][y][3] > 0) {

                    if (lowestDis == 0) {
                        lowestDis = grid[x][y][3];
                        lowestFinDis = grid[x][y][2];
                        lowestX = x;
                        lowestY = y;

                    } else if (grid[x][y][3] < lowestDis) {
                        lowestDis = grid[x][y][3];
                        lowestFinDis = grid[x][y][2];
                        lowestX = x;
                        lowestY = y;
                    } else if (grid[x][y][3] == lowestDis) {
                        if (grid[x][y][2] < lowestFinDis) {
                            lowestDis = grid[x][y][3];
                            lowestFinDis = grid[x][y][2];
                            lowestX = x;
                            lowestY = y;
                        }
                    }
                }
            }
            if (map.finishReached) break;
        }

        if (map.finishReached == false) {
            this.x = lowestX;
            this.y = lowestY;
//            checkAround(x,y);
        } else {
            showFastestRoute();
        }
    }


    private void findNextMove_Dijkstra () {

        int lowestG_Cost = 0;
        int lowestX = -1;
        int lowestY = -1;

        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid.length; y++) {

                if (this.x == map.finishX && this.y == map.finishY) {
                    map.tracking[this.x][this.y][0] = true;
                    map.finishReached = true;
                    break;
                    //Finish reached
                    //Start going back
                }

                // If not actual moves
                if (map.tracking[x][y][0] == false && grid[x][y][1] > 0) {

                    if (lowestG_Cost == 0) {
                        lowestG_Cost = grid[x][y][1];
                        lowestX = x;
                        lowestY = y;

                    } else if (grid[x][y][1] < lowestG_Cost) {
                        lowestG_Cost = grid[x][y][1];
                        lowestX = x;
                        lowestY = y;
                    }
                }
            }
            if (map.finishReached) break;
        }

        if (map.finishReached == false) {
            this.x = lowestX;
            this.y = lowestY;
//            checkAround(x,y);
        } else {
            showFastestRoute();
        }
    }


    private void showFastestRoute () {
        int counting = 2;

        int x = map.finishX;
        int y = map.finishY;
        map.tracking[x][y][1] = true;

        for (int i = 0; i < counting; i++) {
            if (x == map.startX && y == map.startY) break;
            else {

                int tempX = x;

                x = grid[x][y][4];
                y = grid[tempX][y][5];

                map.tracking[x][y][1] = true;

                counting++;
            }


        }



    }

}