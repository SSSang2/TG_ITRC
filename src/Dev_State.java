import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class Dev_State implements Runnable{
	
    private GpioPinDigitalOutput pin1;
    private GpioPinDigitalOutput pin2;
    private GpioPinDigitalOutput pin3;
	
	public Dev_State(){
	}
	public void run(){
		System.out.println(" ***********************************************");
		System.out.println(" *************** DEV STATE CHECK ***************");
		System.out.println(" ***********************************************");
		
		GpioController gpio = GpioFactory.getInstance();
		pin1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "PinLED", PinState.HIGH);
        pin2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "PinLED", PinState.LOW);
        pin3 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "PinLED", PinState.LOW);
		while(true){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	public void TurnOn(String pin){
		if(pin.equals("RED")){
			pin1.high();
		}
		else if(pin.equals("YELLOW")){
			pin2.high();
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			pin2.low();;
		}
		else if(pin.equals("GREEN")){
			pin3.high();
			System.out.println("*********** GREEN TURNED ON *************");
		}
	}
	
	public void TurnOff(String pin){
		if(pin.equals("RED"))
			pin1.low();
		else if(pin.equals("GREEN"))
			pin3.low();
	}
	
}
