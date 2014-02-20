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
    private int[] qLen;

    @Override
    public TransmissionInfo TimeslotAvailable(MediumState previousMediumState, int controlInformation, int localQueueLength) {
        TransmissionInfo info = null;

        if(state == STATE_IDENTIFYING) {
            info = identify(previousMediumState, controlInformation, localQueueLength);

            if(slot >= maxIdentification) {
                state = STATE_SENDING;
                clients = currentId;
                qLen = new int[clients];
            }
        } else if(state == STATE_SENDING) {
            info = send(previousMediumState, controlInformation, localQueueLength);
        }

        slot++;

        return info;
    }

    private TransmissionInfo send(MediumState previousMediumState, int controlInformation, int localQueueLength) {
        try {
        System.out.print(slot + ": ");
        for(int i = 0; i < clients; i++) {
            qLen[i]++;
            System.out.print(qLen[i] + " ");
        }

        if(previousMediumState == MediumState.Succes) {
            int client = controlInformation % clients;
            int qLen = controlInformation / clients;
            this.qLen[client] = qLen;
        }

        int biggest = -1;
        int biggestValue = -1;

        for(int i = 0; i < clients; i++) {
            if(this.qLen[i] > biggestValue) {
                biggest = i;
                biggestValue = this.qLen[biggest];
            }
        }

        System.out.println(" -> " + biggest + " at " + biggestValue);

        if(biggest == id) {
            if(localQueueLength == 0) {
                return new TransmissionInfo(TransmissionType.NoData, id + clients * localQueueLength);
            } else {
                return new TransmissionInfo(TransmissionType.Data, id + clients * localQueueLength);
            }
        } else {
            return new TransmissionInfo(TransmissionType.Silent, 0);
        }
        } catch(ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
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
