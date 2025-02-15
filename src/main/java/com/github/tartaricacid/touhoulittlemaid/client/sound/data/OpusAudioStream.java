package com.github.tartaricacid.touhoulittlemaid.client.sound.data;

import io.github.jaredmdobson.concentus.OpusDecoder;
import io.github.jaredmdobson.concentus.OpusException;
import net.minecraft.client.sounds.AudioStream;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.opus.OpusAudioData;
import org.gagravarr.opus.OpusFile;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class OpusAudioStream implements AudioStream {
    /**
     * Opus 支持的帧长有：2.5ms、5ms、10ms、20ms、40ms、60ms
     * <p>
     * 因为我们对延迟不是很敏感，所以选 60ms
     */
    private static final int MAX_FRAME_SIZE = 60;

    private final InputStream stream;
    private final AudioFormat format;
    private final int frameSize;
    private final byte[] frame;

    public OpusAudioStream(byte[] data) throws IOException, OpusException {
        // 为了方便，我们直接一口气把它全部解码了
        try (OpusFile opusFile = new OpusFile(new OggFile(new ByteArrayInputStream(data)));
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            int sampleRate = opusFile.getInfo().getSampleRate();
            int channels = opusFile.getInfo().getNumChannels();
            int frameSize = sampleRate * MAX_FRAME_SIZE / 1000;

            OpusDecoder decoder = new OpusDecoder(sampleRate, channels);
            byte[] pcmBytes = new byte[sampleRate * channels * 2];
            OpusAudioData packet;

            while ((packet = opusFile.getNextAudioPacket()) != null) {
                byte[] packetBytes = packet.getData();
                int packetLength = packetBytes.length;
                int samplesDecoded = decoder.decode(packetBytes, 0, packetLength, pcmBytes, 0, frameSize, false);
                outputStream.write(pcmBytes, 0, samplesDecoded * channels * 2);
            }
            this.stream = new ByteArrayInputStream(outputStream.toByteArray());
            this.format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16,
                    channels, channels * 2, sampleRate, false);
            this.frameSize = this.format.getFrameSize();
            this.frame = new byte[this.frameSize];
        }
    }

    @Override
    public AudioFormat getFormat() {
        return format;
    }

    /**
     * 从流中读取音频数据，并返回一个最多包含指定字节数的字节缓冲区。
     * 该方法从流中读取音频帧并将其添加到输出缓冲区，直到缓冲区至少
     * 包含指定数量的字节或到达流的末尾。
     *
     * @param size 要读取的最大字节数
     * @return 字节缓冲区，最多包含要读取的指定字节数
     * @throws IOException 如果在读取音频数据时发生I/O错误
     */
    @Override
    public ByteBuffer read(int size) throws IOException {
        // 创建指定大小的ByteBuffer
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(size);
        int bytesRead = 0, count = 0;
        // 循环读取数据直到达到指定大小或输入流结束
        do {
            // 读取下一部分数据
            count = this.stream.read(frame);
            // 将读取的数据写入ByteBuffer
            if (count != -1) {
                byteBuffer.put(frame);
            }
        } while (count != -1 && (bytesRead += frameSize) < size);
        // 翻转ByteBuffer，准备进行读取操作
        byteBuffer.flip();
        // 返回包含读取数据的ByteBuffer
        return byteBuffer;
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
