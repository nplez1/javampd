package org.bff.javampd;

import org.bff.javampd.exception.MPDConnectionException;
import org.bff.javampd.exception.MPDResponseException;
import org.bff.javampd.exception.MPDTimeoutException;
import org.bff.javampd.properties.ResponseProperties;
import org.bff.javampd.properties.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author bill
 * @since: 11/22/13 1:37 PM
 */
public class MPDSocket {
    private static Logger logger = LoggerFactory.getLogger(MPDSocket.class);

    private Socket socket;
    private ResponseProperties responseProperties;
    private ServerProperties commandProperties;
    private String encoding;
    private String lastError;
    private String version;

    private String server;
    private int port;

    private static final int TRIES = 3;

    MPDSocket(InetAddress server, int port, int timeout) throws MPDConnectionException {
        this.server = server.getHostAddress();
        this.port = port;
        this.responseProperties = new ResponseProperties();
        this.commandProperties = new ServerProperties();
        this.encoding = commandProperties.getEncoding();
        this.version = connect(timeout);
    }

    /**
     * If MPD is already connected no attempt will be made to connect and the
     * mpdVersion is returned.
     * <p/>
     * A timeout of 0 means an infinite wait.
     *
     * @param timeout socket timeout, 0 for infinite wait
     * @return the version of MPD
     * @throws java.io.IOException    if there is a socked io problem
     * @throws MPDConnectionException if there are connection issues
     */
    protected synchronized String connect(int timeout) throws MPDConnectionException {
        BufferedReader in;

        this.socket = new Socket();
        SocketAddress sockaddr = new InetSocketAddress(server, port);
        try {
            this.socket.connect(sockaddr, timeout);
        } catch (SocketTimeoutException ste) {
            throw new MPDTimeoutException(ste);
        } catch (IOException e) {
            throw new MPDConnectionException(e);
        }
        String line;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            line = in.readLine();
        } catch (IOException e) {
            throw new MPDConnectionException(e);
        }

        if (isResponseOK(line)) {
            return stripResponse(responseProperties.getOk(), line).trim();
        } else {
            throw new MPDConnectionException("Command from server: " +
                    ((line == null) ? "null" : stripResponse(responseProperties.getError(), line)));
        }
    }

    public synchronized Collection<String> sendCommand(MPDCommand command) throws MPDResponseException {
        byte[] bytesToSend;
        List<String> responseList = new ArrayList<String>();
        OutputStream outStream = null;
        BufferedReader in;

        if (!socket.isConnected()) {
            try {
                connect();
            } catch (Exception e) {
                throw new MPDResponseException("Connection to server lost: " + e.getMessage(), e);
            }
        }

        int count = 0;

        Exception excReturn = null;
        while (count < TRIES) {
            try {
                bytesToSend = convertCommand(command.getCommand(), command.getParams()).getBytes(encoding);

                outStream = socket.getOutputStream();
                outStream.write(bytesToSend);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), encoding));

                String inLine = in.readLine();

                while (inLine != null) {
                    if (isResponseOK(inLine)) {
                        //end of command is ok so break
                        break;
                    }

                    if (isResponseError(inLine)) {
                        throw new MPDResponseException(lastError, command.getCommand());
                    }

                    responseList.add(inLine);

                    inLine = in.readLine();
                }
                count = TRIES + 1;
                return responseList;
            } catch (Exception e) {
                logger.error("Got Error from: {}", command.getCommand(), e);
                for (String str : command.getParams()) {
                    logger.error("\tparam: {}", str);
                }

                if (e instanceof SocketException) {
                    try {
                        connect();
                    } catch (Exception ex) {
                        logger.error("Unable to connect to {} on port {}", server, port, ex);
                    }
                    responseList = new ArrayList<String>();
                    ++count;
                    excReturn = e;
                    logger.error("Retrying command {}", count);
                } else {
                    throw new MPDResponseException(e);
                }
            } finally {
                try {
                    outStream.flush();
                } catch (IOException ex) {
                    logger.error("Unable to flush output stream", ex);
                }
            }
        }

        throw new MPDResponseException(excReturn);
    }

    /**
     * Attempts to connect to MPD with an infinite timeout value.
     * If MPD is already connected no attempt will be made to connect and the
     * mpdVersion is returned.
     *
     * @return return the version of MPD
     * @throws IOException            if there is a socked io problem
     * @throws MPDConnectionException if there are connection issues
     */
    private synchronized String connect() throws MPDConnectionException {
        return connect(0);
    }

    private boolean isResponseOK(final String line) {
        try {
            if (line.startsWith(responseProperties.getOk())) {
                return true;
            }
        } catch (Exception e) {
            logger.error("Could not determine if response is ok", e);
        }
        return false;
    }

    private boolean isResponseError(final String line) {
        try {
            if (line.startsWith(responseProperties.getError())) {
                this.lastError = line.substring(responseProperties.getError().length()).trim();
                return true;
            }
        } catch (Exception e) {
            logger.error("Could not determine if response is error", e);
        }
        return false;
    }

    private String stripResponse(String response, String line) {
        return line.substring(response.length());
    }

    private String convertCommand(String command, List<String> params) {
        StringBuilder sb = new StringBuilder(command);
        if (params != null) {
            for (String param : params) {
                if (param != null) {
                    param = param.replaceAll("\"", "\\\\\"");
                    sb.append(param.contains(" ") ? " \"" : " ");
                    sb.append(param);
                    sb.append(param.contains(" ") ? "\"" : "");
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public synchronized boolean sendCommands(List<MPDCommand> commandList) throws MPDResponseException {
        boolean isOk = true;
        StringBuffer sb = new StringBuffer(convertCommand(commandProperties.getStartBulk(), new ArrayList<String>()));

        for (MPDCommand command : commandList) {
            sb.append(convertCommand(command.getCommand(), command.getParams()));
        }

        sb.append(convertCommand(commandProperties.getEndBulk(), new ArrayList<String>()));


        byte[] bytesToSend;

        OutputStream outStream;
        BufferedReader in;

        if (!socket.isConnected()) {
            try {
                connect();
            } catch (Exception e) {
                throw new MPDResponseException("Connection to server lost: " + e.getMessage(), e);
            }
        }

        try {
            bytesToSend = sb.toString().getBytes(commandProperties.getEncoding());
            outStream = socket.getOutputStream();
            outStream.write(bytesToSend);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String inLine = in.readLine();
            while (inLine != null) {
                if (!inLine.startsWith("list_OK")) {
                    isOk = false;
                }
                if (isResponseOK(inLine)) {
                    //end of command is ok so break
                    break;
                }

                if (isResponseError(inLine)) {
                    throw new MPDResponseException(lastError);
                }
                inLine = in.readLine();
            }
        } catch (Exception e) {
            throw new MPDResponseException(e.getMessage(), e);
        }

        return isOk;
    }

    public String getVersion() {
        return this.version;
    }
}
