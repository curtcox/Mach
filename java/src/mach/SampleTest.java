package mach;

import org.junit.*;

import static mach.Mocks.*;
import static org.junit.Assume.assumeTrue;

public class SampleTest {

    Rocket rocket;
    Pilot pilot;

    interface Rocket {
        void throttle(int value);
        void openAirlock();
        String checkConsoleForInstructions();
        String askMissionControl(String question);
    }

    static class Pilot {
        final Rocket rocket;

        Pilot(Rocket rocket) {
            this.rocket = rocket;
        }

        void act() {
            String instructions = rocket.checkConsoleForInstructions();
            if ("Initiate EVA".equals(instructions)) {
                rocket.askMissionControl("Clear for EVA?");
                rocket.openAirlock();
            }
            rocket.throttle(Integer.MAX_VALUE);
        }
    }

    @Before
    public void setUp() {
        assumeTrue(ShouldRun.Mach);
    }

    @Before
    public void init() {
        Mocks.init(this);
        pilot = new Pilot(rocket);
    }

    @Test
    public void pilot_sets_throttle_to_full_when_launch_instruction_given() {
        _("Launch"); rocket.checkConsoleForInstructions();

        go(); pilot.act();

        verify();
    }

    @Test
    public void pilot_confirms_EVA_before_initiating() {
        _("Initiate EVA"); rocket.checkConsoleForInstructions();
        _("Affirmative"); rocket.askMissionControl("Clear for EVA?");
        _(); rocket.openAirlock();
        no(); _(); wild(0); rocket.throttle(0);

        go(); pilot.act();

        verify();
    }

    @Test
    public void pilot_does_not_touch_throttle() {
        _("Initiate EVA"); rocket.checkConsoleForInstructions();
        _("Affirmative"); rocket.askMissionControl("Clear for EVA?");
        _(); rocket.openAirlock();
        no(); wild(0); rocket.throttle(0);

        go(); pilot.act();

        verify();
    }

}
