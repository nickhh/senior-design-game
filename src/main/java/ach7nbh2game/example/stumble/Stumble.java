/* blacken - a library for Roguelike games
 * Copyright © 2010, 2011 Steven Black <yam655@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package ach7nbh2game.example.stumble;

import com.googlecode.blacken.colors.ColorNames;
import com.googlecode.blacken.colors.ColorPalette;
import com.googlecode.blacken.core.Random;
import com.googlecode.blacken.grid.Grid;
import com.googlecode.blacken.grid.Point;
import com.googlecode.blacken.swing.SwingTerminal;
import com.googlecode.blacken.terminal.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

/**
 * A super-simple game
 * 
 * @author Steven Black
 */
public class Stumble {
    private static final Logger LOGGER = LoggerFactory.getLogger(Stumble.class);
    /**
     * TerminalInterface used by the example
     */
    protected CursesLikeAPI term;
    /**
     * Whether to quit the loop or not
     */
    protected boolean quit;
    private Grid<Integer> grid;
    private Random rand;
    private int nextLocation;
    private final static int EMPTY_FLOOR = 0x2e; 
    private final static Point MAP_START = new Point(1, 0);
    private final static Point MAP_END = new Point(-1, 0);
    private Point upperLeft = new Point(0, 0);
    private Point player = new Point(-1, -1);
    private boolean dirtyMsg = false;
    private boolean dirtyStatus = false;
    private String message;
    private float noisePlane;
    private String helpMessage =
"Stumble\n" +
"A stupidly simple Grid demonstration\n" +
"Copyright (C) 2010-2012 Steven Black\n" +
"\n" +
"An example for the Blacken Roguelike Library.\n" +
"\n" +
"Released under the Apache 2.0 License.\n" +
"\n" +
"How to Play\n" +
"============================================================================\n" +
"A representation of a map is shown.  You (the player) are the\n" +
"at sign (@).  The object is to run around collecting the numbers\n" +
"in order.  The numbers have walls around them that only open up\n" +
"if you've collected the previous number.\n" +
"Use the arrow keys to move around.  There are no opponents.\n" +
"\n" +
"Stumble Example Commands\n" +
"============================================================================\n" +
"Ctrl+L : recenter and redisplay the screen\n" +
"j, Down : move down                  | k, Up : move up\n" +
"h, Left : move left                  | l (ell), Right: move right\n" +
"\n" +
"Q, q, Escape : quit\n" +
"\n" +
"L : show my license                  | N : show legal notices\n" +
"\n" +
"? : this help screen\n";

    /**
     * Create a new instance
     */
    public Stumble() {
        grid = new Grid(new Integer(EMPTY_FLOOR), 20, 20);
        rand = new Random();
        noisePlane = rand.nextFloat();
    }

    int[] placeIt(int what) {
        int[] placement = {-1, -1};
        for (int t=0; t < 10000; t++) {
            int x = rand.nextInt(grid.getWidth());
            int y = rand.nextInt(grid.getHeight());
            if (grid.get(y, x) == EMPTY_FLOOR) {
                grid.set(y, x, new Integer(what));
                placement[0] = y;
                placement[1] = x;
                break;
            }
        }
        if (placement[0] == -1) {
            throw new RuntimeException("It took too long to place.");
        }
        return placement;
    }
    
    /**
     * Make a map
     */
    private void makeMap() {

        grid.clear();

        //for (int c = 0; c < 100; c++) {
        //    placeIt(0x23);
        //}

        //nextLocation = 0x31;
        //for (int c = 0x31; c < 0x3a; c++) {
        //    placeIt(c);
        //}
        
    }
    
    private void showMap() {

        int ey = MAP_END.getY();
        int ex = MAP_END.getX();
        if (ey <= 0) {
            ey += term.getHeight();
        }
        if (ex <= 0) {
            ex += term.getWidth();
        }
        for (int y = MAP_START.getY(); y < ey; y++) {
            for (int x = MAP_START.getX(); x < ex; x++) {

                int y1 = y + upperLeft.getY() - MAP_START.getY();
                int x1 = x + upperLeft.getX() - MAP_START.getX();
                int what = ' ';
                if (y1 >= 0 && x1 >= 0 && y1 < grid.getHeight() && x1 < grid.getWidth()) {
                    what = grid.get(y1, x1);
                }

                int fclr = 7;
                int bclr = 0;
                EnumSet<CellWalls> walls = EnumSet.noneOf(CellWalls.class);

                //if (what == '@'){
                //    bclr = 0xe4;
                //    fclr = 0;
                //} else if (what == '.') {
                //    fclr = (int)(Math.floor(PerlinNoise.noise(x1, y1, noisePlane) * 10.0F)) + 0xee;
                //} else if (what == '#') {
                //    fclr = (int)(Math.floor(PerlinNoise.noise(x1, y1, noisePlane) * 14.0F)) + 0x58;
                //} else if (what == ' ') {
                //    fclr = (int)(Math.floor(PerlinNoise.noise(x1, y1, noisePlane) * 38.0F));
                //    if (fclr < 0) {
                //        fclr *= -1;
                //    }
                //    if (fclr < 28) {
                //        if (fclr > 14) {
                //            fclr -= 14;
                //        }
                //        fclr += 0x58;
                //        what = '#';
                //    } else {
                //        fclr -= 28;
                //        fclr += 0xee;
                //        what = ':';
                //    }
                //} else if (what >= '0' || what <= '9') {
                //    if (what > nextLocation) {
                //        walls = CellWalls.BOX;
                //    }
                //    bclr = 0x11;
                //    fclr = (what - '0') + 0x4;
                //}

                //term.set(y, x, new String(Character.toChars(what)),
                //        fclr, bclr, EnumSet.noneOf(TerminalStyle.class), walls);

                term.set(y, x, new String(Character.toChars(what)),
                        fclr, bclr, EnumSet.noneOf(TerminalStyle.class), walls);
            }
        }
    }
    
    /**
     * The application loop.
     * @return the quit status
     */
    public boolean loop() {
        makeMap();
        int ch = BlackenKeys.NO_KEY;
        int mod;
        updateStatus();
        movePlayerBy(0,0);
        this.message = "Welcome to Stumble!";
        term.move(-1, -1);
        while (!this.quit) {

            placeIt(0x23);

            if (dirtyStatus) {
                updateStatus();
            }
            updateMessage(false);
            showMap();
            term.setCursorLocation(player.getY() - upperLeft.getY() + MAP_START.getY(), 
                                   player.getX() - upperLeft.getX() + MAP_START.getX());
            this.term.getPalette().rotate(0xee, 10, +1);
            term.refresh();
            mod = BlackenKeys.NO_KEY;
            ch = term.getch();
            if (ch == BlackenKeys.RESIZE_EVENT) {
                this.refreshScreen();
                continue;
            } else if (BlackenKeys.isModifier(ch)) {
                mod = ch;
                ch = term.getch();
            }
            // LOGGER.debug("Processing key: {}", ch);
            if (ch != BlackenKeys.NO_KEY) {
                this.message = null;
                doAction(mod, ch);
            }
        }
        return this.quit;
    }

    private void updateMessage(boolean press) {
        //if (this.message != null && !dirtyMsg) {
        //    dirtyMsg = true;
        //}
        //if (dirtyMsg) {
        //    for (int x = 0; x < term.gridWidth(); x++) {
        //        term.mvaddch(0, x, ' ');
        //    }
        //    if (message == null) {
        //        dirtyMsg = false;
        //    } else {
        //        term.mvputs(0, 0, message);
        //    }
        //    if (press) {
        //        message = null;
        //    }
        //}
    }

    /**
     * Update the status.
     */
    private void updateStatus() {
        //term.setCurForeground(7);
        //dirtyStatus = false;
        //for (int x = 0; x < term.getWidth()-1; x++) {
        //    term.mvaddch(term.getHeight(), x, ' ');
        //}
        //if (nextLocation <= '9') {
        //    term.mvputs(term.getHeight(), 0, "Get the ");
        //    term.setCurForeground((nextLocation - '0') + 0x4);
        //    term.addch(nextLocation);
        //    term.setCurForeground(7);
        //    if (nextLocation == '9') {
        //        term.puts(" to win.");
        //    }
        //} else {
        //    term.mvputs(term.getHeight(), 0, "You won!");
        //}
        //String msg = "? for help.";
        //term.mvputs(term.getHeight(), term.getWidth()-msg.length()-1, msg);
    }

    private void refreshScreen() {
        //term.clear();
        //updateStatus();
        //updateMessage(false);
        //this.showMap();
    }
    
    private boolean doAction(int modifier, int ch) {
        //if (BlackenModifier.MODIFIER_KEY_CTRL.hasFlag(modifier)) {
        //    switch (ch) {
        //    case 'l':
        //    case 'L':
        //        this.recenterMap();
        //        refreshScreen();
        //        break;
        //    }
        //    return false;
        //}
        //switch (ch) {
        //case 'j':
        //case BlackenKeys.KEY_DOWN:
        //case BlackenKeys.KEY_NP_2:
        //case BlackenKeys.KEY_KP_DOWN:
        //    movePlayerBy(+1,  0);
        //    break;
        //case 'k':
        //case BlackenKeys.KEY_UP:
        //case BlackenKeys.KEY_NP_8:
        //case BlackenKeys.KEY_KP_UP:
        //    movePlayerBy(-1,  0);
        //    break;
        //case 'h':
        //case BlackenKeys.KEY_LEFT:
        //case BlackenKeys.KEY_NP_4:
        //case BlackenKeys.KEY_KP_LEFT:
        //    movePlayerBy(0,  -1);
        //    break;
        //case 'l':
        //case BlackenKeys.KEY_RIGHT:
        //case BlackenKeys.KEY_NP_6:
        //case BlackenKeys.KEY_KP_RIGHT:
        //    movePlayerBy(0,  +1);
        //    break;
        //case 'q':
        //case 'Q':
        //case BlackenKeys.KEY_ESCAPE:
        //    this.quit = true;
        //    return false;
        //case 'L':
        //    showMyLicense();
        //    refreshScreen();
        //    break;
        //case 'N':
        //    showLegalNotices();
        //    refreshScreen();
        //    break;
        //case 'F':
        //    showFontLicense();
        //    refreshScreen();
        //    break;
        //case '?':
        //    showHelp();
        //    refreshScreen();
        //    break;
        //default:
        //    return false;
        //}
        return true;
    }

    /**
     * Move the player by an offset
     * 
     * @param y row offset (0 stationary)
     * @param x column offset (0 stationary)
     */
    private void movePlayerBy(int y, int x) {
        //Integer there;
        //if (player.getY() == -1) {
        //    int[] pos = placeIt('@');
        //    player.setPosition(pos[0], pos[1]);
        //    recenterMap();
        //    return;
        //}
        //Positionable oldPos = new Point(player);
        //try {
        //    there = grid.get(player.getY() + y, player.getX() + x);
        //} catch(IndexOutOfBoundsException e) {
        //    return;
        //}
        //if (there == EMPTY_FLOOR || there == nextLocation) {
        //    grid.set(oldPos.getY(), oldPos.getX(), EMPTY_FLOOR);
        //    player.setPosition(player.getY() + y, player.getX() + x);
        //    grid.set(player.getY(), player.getX(), 0x40);
        //    int playerScreenY = player.getY() - upperLeft.getY() + MAP_START.getY();
        //    int playerScreenX = player.getX() - upperLeft.getX() + MAP_START.getX();
        //    int ScreenY2 = (MAP_END.getY() <= 0
        //            ? term.gridHeight() -1 + MAP_END.getY() : MAP_END.getY());
        //    int ScreenX2 = (MAP_END.getX() <= 0
        //            ? term.gridWidth() -1 + MAP_END.getX() : MAP_END.getX());
        //    if (playerScreenY >= ScreenY2 || playerScreenX >= ScreenX2 ||
        //            playerScreenY <= MAP_START.getY() ||
        //            playerScreenX <= MAP_START.getX()) {
        //        recenterMap();
        //    }
        //    if (there == nextLocation) {
        //        StringBuilder buf = new StringBuilder();
        //        buf.append("Got it.");
        //        buf.append(' ');
        //        if (there == '9') {
        //            buf.append("All done!");
        //        } else {
        //            buf.append("Next is unlocked.");
        //        }
        //        nextLocation ++;
        //        this.message = buf.toString();
        //        dirtyStatus = true;
        //        this.updateMessage(false);
        //    }
        //} else if (there >= '0' && there <= '9') {
        //    this.message = "That position is still locked.";
        //    this.updateMessage(false);
        //}
    }

    private void recenterMap() {
        //upperLeft.setY(player.getY() - (term.gridHeight()-2)/2);
        //upperLeft.setX(player.getX() - (term.gridWidth()-2)/2);
    }
    
    
    /**
     * Initialize the example
     * 
     * @param term alternate TerminalInterface to use
     * @param palette alternate ColorPalette to use
     */
    public void init(TerminalInterface term, ColorPalette palette) {
        if (term == null) {
            term = new SwingTerminal();
            term.init("Blacken Example: Stumble", 25, 80);
        }
        this.term = new CursesLikeAPI(term);
        if (palette == null) {
            palette = new ColorPalette();
            palette.addAll(ColorNames.XTERM_256_COLORS, false);
            palette.putMapping(ColorNames.SVG_COLORS);
        } 
        this.term.setPalette(palette);
    }
    
    /**
     * Start the example
     * 
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        Stumble that = new Stumble();
        that.init(null, null);
        that.loop();
        that.quit();
    }
    
    /**
     * Quit the application.
     * 
     * <p>This calls quit on the underlying TerminalInterface.</p>
     */
    public void quit() {
        term.quit();
    }

    private void showLegalNotices() {
        // show Notices file
        // This is the only one that needs to be shown for normal games.
        //ViewerHelper vh;
        //vh = new ViewerHelper(term, "Legal Notices", Obligations.getBlackenNotice());
        //vh.setColor(7, 0);
        //vh.run();
    }

    private void showFontLicense() {
        // show the font license
        //ViewerHelper vh;
        //new ViewerHelper(term,
        //        Obligations.getFontName() + " Font License",
        //        Obligations.getFontLicense()).run();
    }

    private void showHelp() {
        //ViewerHelper vh;
        //vh = new ViewerHelper(term, "Help", helpMessage);
        //vh.setColor(7, 0);
        //vh.run();
    }

    private void showMyLicense() {
        // show Apache 2.0 License
        //ViewerHelper vh;
        //vh = new ViewerHelper(term, "License", Obligations.getBlackenLicense());
        //vh.setColor(7, 0);
        //vh.run();
    }

}