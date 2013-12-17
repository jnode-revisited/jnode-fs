/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.driver.bus.ide.command;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jnode.driver.bus.ide.IDEBus;
import org.jnode.driver.bus.ide.IDEIO;
import org.jnode.util.NumberUtils;
import org.jnode.util.TimeoutException;

/**
 * @author epr
 *         test version
 */
public class IDEReadSectorsCommand extends IDERWSectorsCommand {
    private final ByteBuffer buf;

    private static final Logger log = Logger.getLogger(IDEReadSectorsCommand.class);

    private int readSectors = 0;

    public IDEReadSectorsCommand(
            boolean primary, 
            boolean master,
            boolean is48bit,
            long lbaStart, 
            int sectors, 
            ByteBuffer dest) {
        super(primary, master, is48bit, lbaStart, sectors);
        buf = dest;
    }

    /**
     * @see org.jnode.driver.bus.ide.IDECommand#setup(IDEBus, IDEIO)
     */
    protected void setup(IDEBus ide, IDEIO io) throws TimeoutException {
        super.setup(ide, io);
        io.setCommandReg(is48bit ? CMD_READ_EXT : CMD_READ);
    }

    /**
     * @see org.jnode.driver.bus.ide.IDECommand#handleIRQ(IDEBus, IDEIO)
     */
    protected void handleIRQ(IDEBus ide, IDEIO io) {
        final int state = io.getStatusReg(); // Read status, flush IRQ
        log.debug("RdSect IRQ : st=" + NumberUtils.hex(state));
        if ((state & ST_ERROR) != 0) {
            setError(io.getErrorReg());
        } else {
            if ((state & (ST_BUSY | ST_DATA_REQUEST)) == ST_DATA_REQUEST) {
                // final int offset = readSectors * SECTOR_SIZE;
                for (int i = 0; i < 256; i++) {
                    final int v = io.getDataReg();
                    buf.put((byte) (v & 0xFF));
                    buf.put((byte) ((v >> 8) & 0xFF));
                    /*if (i > 253) {
                    	log.info("Read [" + i + "]=" + v);
                    }*/
                }
                readSectors++;
                if (readSectors == sectorCount) {
                    notifyFinished();
                }
            }
        }
    }
}
