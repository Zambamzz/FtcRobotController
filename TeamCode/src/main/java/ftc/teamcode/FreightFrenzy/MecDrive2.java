package ftc.teamcode.FreightFrenzy;

//import com.qualcomm.hardware.lynx.LynxDcMotorController;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcontroller.external.samples.SensorREV2mDistance;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.AccerlationControlledDrivetrainPowerGeneratorForAuto;
import org.firstinspires.ftc.teamcode.Globals;
import org.firstinspires.ftc.teamcode.TrackingWheelIntegrator;
import org.firstinspires.ftc.teamcode.control.MecanumDrive;
import org.firstinspires.ftc.teamcode.robotComponents.drivebase.SkyStoneDriveBase;

@TeleOp
public class MecDrive2 extends LinearOpMode {


        private DcMotorEx FL;
        private DcMotorEx FR;
        private DcMotorEx RL;
        private DcMotorEx RR;
        private Servo servo1;
        private Servo servo2;
        private Servo clawServo;
        private DistanceSensor beambreak;
        private double leftStickX;
        private double leftStickY;
        private double rightStickX;
        private boolean beamstat;
        double motor_speed;
        //MaxSonarI2CXL sensor;

        SkyStoneDriveBase skyStoneDriveBase;
        //OpenCvCamera camera;

        AccerlationControlledDrivetrainPowerGeneratorForAuto acclCtrl;
        TrackingWheelIntegrator trackingWheelIntegrator = new TrackingWheelIntegrator();

    private double maxPos = 1;
    private double minPos = 0;


//code for lift

    private double lPos = 0.2;
    private double rPos = 0.81;
    private double cPos = 0;
    private double crPos = 1;

    private double coneLVL = .01;
    private double rconeLVL = 0.99;

    private double lowJunc = 0.54;
    private double rlowJunc = 0.47;

    private double midJunc = 0.68;
    private double rmidJunc = 0.33;

    private double highJunc = 0.28;
    private double rhighJunc =  0.738;

    //claw Servo init + var

    private double clClose = 0;
    private double cl = clClose;
    private double clOpen = 1;

        // LynxDcMotorController ctrl;
        LynxModule module;

        @Override
        public void runOpMode() throws InterruptedException {


            trackingWheelIntegrator = new TrackingWheelIntegrator();

            //mapping out the robot
            FL= (DcMotorEx) hardwareMap.get(DcMotor.class, "FL");
            FR= (DcMotorEx) hardwareMap.get(DcMotor.class, "FR");
            RL= (DcMotorEx) hardwareMap.get(DcMotor.class, "RL");
            RR= (DcMotorEx) hardwareMap.get(DcMotor.class, "RR");
            servo1= (Servo) hardwareMap.get(Servo.class, "servo1");
            servo2= (Servo) hardwareMap.get(Servo.class, "servo2");
            clawServo= (Servo) hardwareMap.get(Servo.class, "clawServo");
            beambreak= (DistanceSensor) hardwareMap.get(DistanceSensor.class, "beambreak");
            //sensor= (MaxSonarI2CXL) hardwareMap.get(MaxSonarI2CXL.class, "sensor");
            skyStoneDriveBase = new SkyStoneDriveBase();
            motor_speed = 0;
            skyStoneDriveBase.init(hardwareMap);
            skyStoneDriveBase.resetEncoders();
            skyStoneDriveBase.enableBrake(true);
            skyStoneDriveBase.enablePID();
            Globals.robot=skyStoneDriveBase;
            Globals.driveBase=skyStoneDriveBase;
            Globals.trackingWheelIntegrator = trackingWheelIntegrator;
            Globals.odoModule = module;
            Globals.opMode = this;
            Globals.robot.enableBrake(true);

            boolean AutomationLastState = false;
            acclCtrl = new AccerlationControlledDrivetrainPowerGeneratorForAuto(.08, 1, .05);

            telemetry.setMsTransmissionInterval(20);
            telemetry.addData("Status", "Initialized");
            telemetry.update();

            waitForStart();

            while (opModeIsActive()) {

                if (gamepad2.x) {         //If button is pressed Auto aline will run. if not normally gamepad works
                    if (!AutomationLastState) {
                        //clearEnc();
                        trackingWheelIntegrator.setFirstTrackingVal(0,0);
                        //buildTrajectory();

                    }
                    AutomationLastState = true;


                }
                else {
                    runGamepad();
                    if (AutomationLastState) {
                        AutomationLastState = false;
                        Globals.robot.enableBrake(true);
                        Globals.robot.disablePID();



                    }
                }
            }
        }

        void runGamepad() {

            if (gamepad1.left_stick_x > .01) {
                leftStickX = gamepad1.left_stick_x;
            }
            else if (gamepad1.left_stick_x < -.01) {
                leftStickX = gamepad1.left_stick_x;
            }
            else {
                leftStickX = 0;
            }
            if (gamepad1.left_stick_y > .01 ) {
                leftStickY = gamepad1.left_stick_y;
            }
            else if (gamepad1.left_stick_y < -.01) {
                leftStickY = gamepad1.left_stick_y;
            }
            else {
                leftStickY = 0;

            }
            if (gamepad1.right_stick_x > .01 ) {
                rightStickX = gamepad1.right_stick_x;
            }
            else if (gamepad1.right_stick_x < -.01) {
                rightStickX = gamepad1.right_stick_x;
            }
            else {
                rightStickX = 0;

            }

            telemetry.addData("FL", FL.getCurrentPosition());
            telemetry.addData("FR", FR.getCurrentPosition());
            telemetry.addData("RL", RL.getCurrentPosition());
            telemetry.addData("RR", RR.getCurrentPosition());
            telemetry.addData("beam", beambreak.getDistance(DistanceUnit.CM));
            telemetry.addData("brokenbeam", beamstat);
            if (beambreak.getDistance(DistanceUnit.CM) < 20) {
                beamstat = true;
            }

            telemetry.update();

            // Edit this block to change the speed (always keep rightStickX below the others)
            MecanumDrive.cartesian(Globals.robot,
                    -leftStickY * .25, // Main
                    leftStickX * .25 , // Strafe
                    rightStickX * .20); // Turn



            if (gamepad1.triangle) {
                clawServo.setPosition(-0.2);
            }
            if (gamepad1.square) {
                clawServo.setPosition(.4);
            }
            //clawServo.setPosition(.4);
           // if (lPos > lowJunc)
           // {
                if (beambreak.getDistance(DistanceUnit.CM) > 7.5 && beambreak.getDistance(DistanceUnit.CM) < 12)
                    gamepad1.rumble(250);
                //}


            //lift positions

            servo2.setPosition(rPos);
            servo1.setPosition(lPos);

            if(gamepad1.right_bumper) {
                lPos = coneLVL;
                rPos = rconeLVL;

            }
            if(gamepad1.left_bumper) {
                lPos = lowJunc;
                rPos = rlowJunc;

            }
            if(gamepad1.left_trigger == 1) {
                lPos = midJunc;
                rPos= rmidJunc;
            }
            if(gamepad1.right_trigger == 1) {
                lPos = highJunc;
                rPos = rhighJunc;
            }

            if(gamepad1.dpad_up) {
                if(lPos < maxPos) {
                    lPos = lPos + .02;
                    rPos = rPos - .02;

                }

            }
            if(gamepad1.dpad_down) {
                if (lPos > minPos) {
                    lPos = lPos - .02;
                    rPos = rPos + .02;
                }
            }

            telemetry.addData("lvldiff", coneLVL);
            telemetry.addData("lPos", servo1.getPosition());
            telemetry.addData("rPos:", servo2.getPosition());
            //telemetry.addData("Sensor reading", sensor_dist);
            telemetry.addData("Motor Speed", motor_speed);



    }
}
