package cat.ieti.ietigame;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketListener;
import com.github.czyzby.websocket.WebSockets;

import java.util.HashMap;
import java.util.Map;

public class Level1Screen implements Screen {
    final IetiGame game;
    String userid = "";

    Texture background;
    TextureRegion bgRegion;
    public int posx;
    public int posy;
    public final int centre_person_x;
    public final int centre_person_y;

    Animation<TextureRegion> walkLeft;
    Animation<TextureRegion> walkRight;
    Animation<TextureRegion> walkUp;
    Animation<TextureRegion> walkDown;
    float stateTime;
    float lastSendTime;
    // personatge
    Texture ietiSheet;
    int FRAME_ROWS = 5;
    int FRAME_COLS = 4;

    // controls i trets
    final int IDLE=0, UP=1, DOWN=2, LEFT=3, RIGHT=4;
    final int TRET_WIDTH = 10;
    final int TRET_HEIGHT = 10;
    Rectangle up, down, left, right, fire;

    // comunicacions
    WebSocket socket;
    String address = "10.0.2.2"; // PC localhost on Android
    int port = 8888;
    static class PosMessage {
        public int posx;
        public int posy;
        public String id;
    }
    HashMap<String,PosMessage> enemics = new HashMap<>();

    public Level1Screen(final IetiGame game) {
        this.game = game;

        if( Gdx.app.getType()== Application.ApplicationType.Desktop )
            address = "localhost";
        // Connect to server
        System.out.println("Connectant al servidor...");
        socket = WebSockets.newSocket(WebSockets.toWebSocketUrl(address, port));
        socket.setSendGracefully(false);
        socket.addListener((WebSocketListener) new IetiWSListener());
        socket.connect();

        socket.send("enviant dades");

        // background
        background = new Texture(Gdx.files.internal("polar-background.png"));
        background.setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);
        bgRegion = new TextureRegion(background);
        posx = 0;
        posy = 0;

        // personatge IETI
        ietiSheet = new Texture("ieti-walk.png");
        TextureRegion tmp[][] = TextureRegion.split( ietiSheet,
                                ietiSheet.getWidth()/FRAME_COLS,
                                ietiSheet.getHeight()/FRAME_ROWS);

        // hi ha una animació en cada fila (i)
        for( int i=0; i<FRAME_ROWS; i++ ) {
            TextureRegion frames[];
            frames = new TextureRegion[FRAME_COLS];
            int index = 0;
            for (int j = 0; j < FRAME_COLS; j++) {
                frames[index++] = tmp[i][j];
            }
            // Guardem animació
            // Animation: 0.25 és el període de duració del cada frame
            float periode = 0.15f;
            switch( i ) {
                case 0:
                    walkDown = new Animation<TextureRegion>(periode, frames);
                    break;
                case 1:
                    walkRight = new Animation<TextureRegion>(periode, frames);
                    break;
                case 2:
                    walkUp = new Animation<TextureRegion>(periode, frames);
                    break;
                case 3:
                    walkLeft = new Animation<TextureRegion>(periode, frames);
                    break;
                default:
                    break;
            }
        }
        stateTime = lastSendTime = 0.0f;

        // Comandament
        // facilities per calcular interseccions
        up = new Rectangle(0,game.SCR_HEIGHT*2/3,game.SCR_WIDTH,game.SCR_HEIGHT/3);
        down = new Rectangle(0,0,game.SCR_WIDTH,game.SCR_HEIGHT/3);
        left = new Rectangle(0,0,game.SCR_WIDTH/3,game.SCR_HEIGHT);
        right = new Rectangle(game.SCR_WIDTH*2/3,0,game.SCR_WIDTH/3,game.SCR_HEIGHT);
        //fire = new Circle( game.SCR_WIDTH-50,50,50 );

        // posicio central personatge (fixa)
        TextureRegion sampleRegion = tmp[0][0];
        centre_person_x = game.SCR_WIDTH/2 - sampleRegion.getRegionWidth()/2;
        centre_person_y = game.SCR_HEIGHT/2 - sampleRegion.getRegionHeight()/2;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1,1,1,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.camera.update();
        stateTime += delta;
        // default frame (IDLE) : DOWN
        TextureRegion frame = walkDown.getKeyFrames()[0];

        // CONTROLS
        // input controls (pref: joystick / keys)
        int move = virtual_joystick_control();
        if(move==IDLE)
            move = keys_control();

        // CALCULA
        switch (move) {
            case LEFT:
                frame = walkLeft.getKeyFrame(stateTime, true);
                posx -= 2;
                break;
            case RIGHT:
                frame = walkRight.getKeyFrame(stateTime, true);
                posx += 2;
                break;
            case UP:
                frame = walkUp.getKeyFrame(stateTime, true);
                posy += 2;
                break;
            case DOWN:
                frame = walkDown.getKeyFrame(stateTime, true);
                posy -= 2;
                break;
            case IDLE:
                break;
        }

        // boradcast posició cada 1/4 de segon
        if( stateTime-lastSendTime>0.25f ) {
            lastSendTime = stateTime;
            if( socket!=null ) {
                PosMessage posMessage = new PosMessage();
                posMessage.posx = posx;
                posMessage.posy = posy;
                ObjectMapper mapper = new ObjectMapper();
                try {
                    socket.send(mapper.writeValueAsString(posMessage));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            else {
                System.out.println("error en socket");
            }
        }

        // PINTA
        // inici
        game.batch.setProjectionMatrix(game.camera.combined);
        game.batch.begin();
        // pinta background
        bgRegion.setRegion(posx,-posy,game.SCR_WIDTH,game.SCR_HEIGHT);
        game.batch.draw(bgRegion,0,0);
        // pinta personatge
        game.batch.draw(frame,centre_person_x,centre_person_y);
        // pinta enemics
        for( PosMessage enemic : enemics.values() ) {
            int localx = centre_person_x + enemic.posx - posx;
            int localy = centre_person_y + enemic.posy - posy;
            TextureRegion enemicFrame = walkDown.getKeyFrames()[0];
            game.batch.draw(enemicFrame,localx,localy);
        }
        // pinta controls
        //game.batch.draw(control,0,0);
        //game.batch.draw(control2,game.SCR_WIDTH-control2.getWidth(),0);
        // fi pintar
        game.batch.end();

        // Final de nivell
        if( Gdx.input.isKeyPressed(Input.Keys.ESCAPE) ) {
            game.setScreen(new MainMenuScreen(game) );
            dispose();
        }
    }

    protected int keys_control() {
        // triem animació i movem background segons la tecla premuda
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            return LEFT;
        } else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            return RIGHT;
        } else if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
            return UP;
        } else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            return DOWN;
        }
        // no key
        return IDLE;
    }

    @SuppressWarnings("SuspiciousIndentation")
    protected int virtual_joystick_control() {
        // iterar per multitouch
        for(int i=0;i<10;i++)
        if (Gdx.input.isTouched(i)) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(i), Gdx.input.getY(i), 0);
            // traducció de coordenades reals (depen del dispositiu) a 800x480
            game.camera.unproject(touchPos);
            if (up.contains(touchPos.x, touchPos.y)) {
                return UP;
            } else if (down.contains(touchPos.x, touchPos.y)) {
                return DOWN;
            } else if (left.contains(touchPos.x, touchPos.y)) {
                return LEFT;
            } else if (right.contains(touchPos.x, touchPos.y)) {
                return RIGHT;
            }
        }
        return IDLE;
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
        // TODO: music play
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

    // COMUNICACIONS (rebuda)
    /////////////////////////////////////////////

    class IetiWSListener implements WebSocketListener {

        @Override
        public boolean onOpen(WebSocket webSocket) {
            System.out.println("Opening...");
            return false;
        }

        @Override
        public boolean onClose(WebSocket webSocket, int closeCode, String reason) {
            System.out.println("Closing...");
            return false;
        }

        @Override
        public boolean onMessage(WebSocket webSocket, String packet) {
            System.out.println("Message: "+packet);
            if( packet.startsWith("Benvingut") ) {
                String[] parts = packet.split("=");
                userid = parts[1];
                System.out.println("USERID="+userid);
            }
            // TODO desconnexions de "contrincants"
            else {
                // posició de "contrincants"
                ObjectMapper mapper = new ObjectMapper();
                try {
                    PosMessage pos = mapper.readValue(packet,PosMessage.class);
                    if( pos.id.equals(userid) ) {
                        // es un missatge propi nostre, el descartem
                        return false;
                    }
                    // ho guardem en un Map i així no es dupliquen entrades
                    enemics.put(pos.id,pos);
                    System.out.println("Enemic a x="+pos.posx+" y="+pos.posy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        public boolean onMessage(WebSocket webSocket, byte[] packet) {
            System.out.println("Message:");
            return false;
        }

        @Override
        public boolean onError(WebSocket webSocket, Throwable error) {
            System.out.println("ERROR:"+error.toString());
            return false;
        }
    }
}
