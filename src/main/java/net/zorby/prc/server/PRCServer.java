package net.zorby.prc.server;

import net.minecraft.util.math.ColumnPos;
import net.zorby.prc.PRC;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Scanner;

public class PRCServer {

    // libcubiomes enum variant MC_1_20
    private static final int MC_VERSION = 25;

    public final Logger logger = LogManager.getLogger("prc/server");

    private Process process;

    public PRCServer() { }

    public void restart(long seed) throws IOException {
        logger.info("restarting...");

        if (this.process != null) {
            this.process.destroy();
        }

        this.start(seed);
    }

    public ColumnPos request(int x, int z) throws IOException {
        OutputStream outStream = this.process.getOutputStream();
        InputStream inStream  = this.process.getInputStream();

        ByteBuffer request = ByteBuffer.allocate(8);
        request.order(ByteOrder.LITTLE_ENDIAN);

        request.putInt(x);
        request.putInt(z);

        outStream.write(request.array());
        outStream.flush();

        int elytraX, elytraZ;
        while (true) {
            ByteBuffer response;
            try {
                response = ByteBuffer.wrap(inStream.readNBytes(9));
                response.order(ByteOrder.LITTLE_ENDIAN);
                System.out.println(Arrays.toString(response.array()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            byte success = response.get();
            if (success != 1) {
                System.out.println("no end cities in range :((((");
                return null;
            }

            elytraX = response.getInt();
            elytraZ = response.getInt();

            boolean hasEntry = PRC.getInstance().getDatabase().hasEntry(elytraX, elytraZ);

            outStream.write((byte) (hasEntry ? 0 : 1));
            outStream.flush();

            if (!hasEntry) break;
        }

        return new ColumnPos(elytraX, elytraZ);
    }

    private void start(long seed) throws IOException {
        this.process = new ProcessBuilder("prcserver.exe").start();

        new Thread(() -> {
            Scanner scanner = new Scanner(this.process.getErrorStream());

            while (scanner.hasNextLine()) {
                logger.info("[stderr] " + scanner.nextLine());
            }
        }, "PRC Server").start();

        this.sendHeader(seed);
    }

    private void sendHeader(long seed) throws IOException {
        OutputStream stream = this.process.getOutputStream();

        ByteBuffer header = ByteBuffer.allocate(12);
        header.order(ByteOrder.LITTLE_ENDIAN);

        header.putInt(MC_VERSION);

        header.putLong(seed);

        stream.write(header.array());
        stream.flush();
    }
}
