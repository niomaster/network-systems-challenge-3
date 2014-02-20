package protocol;

import java.util.Random;

/**
 * Created by Sophie on 2/20/14.
 */
public class UberProtocol implements IMACProtocol {
    public static final int STATE_IDENTIFYING = 0;
    public static final int STATE_SENDING = 1;

    public static final int MAX_IDENTIFICATION_WINDOW = 4;

    public static final Random RANDOM = new Random();

    private static final int CONTROL_MESSAGE_IDENTIFY = 1;
    private static final int CONTROL_MESSAGE_DONE = 2;

    private int state = STATE_IDENTIFYING;
    private int slot = 0;
    private int currentId = 0;
    private int maxIdentification = slot + MAX_IDENTIFICATION_WINDOW;

    private int selectedIdentifySlot = -1;
    private int id;
    private int clients;
    private boolean hadData = false;
    private boolean claiming = false;

    @Override
    public TransmissionInfo TimeslotAvailable(MediumState previousMediumState, int controlInformation, int localQueueLength) {
        TransmissionInfo info = null;

        if(state == STATE_IDENTIFYING) {
            info = identify(previousMediumState, controlInformation, localQueueLength);

            if(slot >= maxIdentification) {
                state = STATE_SENDING;
                clients = currentId;
            }
        } else if(state == STATE_SENDING) {
            info = send(previousMediumState, controlInformation, localQueueLength);
        }

        slot++;

        return info;
    }

    private TransmissionInfo send(MediumState previousMediumState, int controlInformation, int localQueueLength) {
//        if(controlInformation >= CONTROL_MESSAGE_DONE) {
//            if(id > controlInformation - CONTROL_MESSAGE_DONE) {
//                id--;
//            }
//
//            if(clients > 1) {
//                clients--;
//            }
//        }

        if(slot % clients == id) {
            if(localQueueLength != 0) {
                hadData = true;
                return new TransmissionInfo(TransmissionType.Data, 0);
            } else {
                if(hadData) {
                    hadData = false;
                    return new TransmissionInfo(TransmissionType.NoData, CONTROL_MESSAGE_DONE + id);
                } else {
                    return new TransmissionInfo(TransmissionType.Silent, 0);
                }
            }
        } else {
            return new TransmissionInfo(TransmissionType.Silent, 0);
        }
    }

    private TransmissionInfo identify(MediumState previousMediumState, int controlInformation, int localQueueLength) {
        if(selectedIdentifySlot == -1) {
            selectedIdentifySlot = slot + RANDOM.nextInt(MAX_IDENTIFICATION_WINDOW);
        }

        if(previousMediumState == MediumState.Collision) {
            if(claiming) {
                selectedIdentifySlot = slot + RANDOM.nextInt(MAX_IDENTIFICATION_WINDOW);
            }

            maxIdentification = slot + MAX_IDENTIFICATION_WINDOW;
        } else if(previousMediumState == MediumState.Succes) {
            if(claiming) {
                id = currentId;
            }

            currentId++;
        }

        claiming = false;

        if(slot == selectedIdentifySlot) {
            claiming = true;
            return new TransmissionInfo(TransmissionType.NoData, CONTROL_MESSAGE_IDENTIFY);
        }

        return new TransmissionInfo(TransmissionType.Silent, 0);
    }
}
