package abstracts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.mygdx.potatoandtomato.PTGame;
import org.junit.BeforeClass;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

/**
 * Created by SiongLeng on 1/12/2015.
 */
public abstract class TestAbstract {

    @BeforeClass
    public static void oneTimeSetUp() {

        Gdx.gl = mock(GL20.class);
        Gdx.gl20 = mock(GL20.class);
        new HeadlessApplication(new PTGame());

//        doAnswer(new Answer<Void>() {
//            @Override
//            public Void answer(InvocationOnMock invocation) throws Throwable {
//                Object[] arguments = invocation.getArguments();
//                RunnableArgs toRun = (RunnableArgs) arguments[0];
//                Game[] arr = new Game[1];
//                Game game = new Game();
//                game.id = "1";
//                game.title = "Test Game";
//                game.version = "1.21";
//                arr[0] = game;
//                toRun.setObject(arr);
//                toRun.run();
//                return null;
//            }
//        }).when(WebApi.services).getGamesList(any(RunnableArgs.class));
//
//        doAnswer(new Answer<Void>() {
//            @Override
//            public Void answer(InvocationOnMock invocation) throws Throwable {
//                Object[] arguments = invocation.getArguments();
//                RunnableArgs toRun = (RunnableArgs) arguments[1];
//                User user = new User("TestUser", User.Race.UNKNOWN);
//                user.token = "123";
//                toRun.setObject(user);
//                toRun.run();
//                return null;
//            }
//        }).when(WebApi.services).createNewUser(anyString(), any(RunnableArgs.class));
//
//        doAnswer(new Answer<Void>() {
//            @Override
//            public Void answer(InvocationOnMock invocation) throws Throwable {
//                Object[] arguments = invocation.getArguments();
//                String token = (String) arguments[0];
//                RunnableArgs toRun = (RunnableArgs) arguments[1];
//                if(token.equals("123")){
//                    User user = new User("TestUser", User.Race.UNKNOWN);
//                    user.token = "123";
//                    toRun.setObject(user);
//                }
//                toRun.run();
//                return null;
//            }
//        }).when(WebApi.services).getUserByToken(anyString(), any(RunnableArgs.class));


    }

}