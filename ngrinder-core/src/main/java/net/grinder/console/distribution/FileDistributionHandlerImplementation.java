//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.grinder.console.distribution;

import java.io.File;
import net.grinder.communication.Address;
import net.grinder.console.communication.DistributionControl;
import net.grinder.console.distribution.AgentSet.OutOfDateException;
import net.grinder.console.distribution.FileDistributionHandler.Result;
import net.grinder.util.FileContents;
import net.grinder.util.FileContents.FileContentsException;

final class FileDistributionHandlerImplementation implements FileDistributionHandler {
    private final CacheParameters m_cacheParameters;
    private final File m_directory;
    private final File[] m_files;
    private final long m_latestFileTime;
    private final DistributionControl m_distributionControl;
    private final AgentSet m_agents;
    private int m_fileIndex = 0;

    FileDistributionHandlerImplementation(CacheParameters cacheParameters, File directory, File[] files, DistributionControl distributionControl, AgentSet agents) {
        this.m_cacheParameters = cacheParameters;
        this.m_directory = directory;
        this.m_files = files;
        this.m_distributionControl = distributionControl;
        this.m_agents = agents;
        long latestFileTime = -1L;

        for(int i = 0; i < this.m_files.length; ++i) {
            long fileTime = (new File(this.m_directory, this.m_files[i].getPath())).lastModified();
            latestFileTime = Math.max(latestFileTime, fileTime);
        }

        this.m_latestFileTime = latestFileTime;
    }

    public Result sendNextFile() throws FileContentsException {
        try {
            if (this.m_fileIndex < this.m_files.length) {
                if (this.m_fileIndex == 0) {
//                    Address addressAgentsWithInvalidCaches = this.m_agents.getAddressOfOutOfDateAgents(0L);
                    Address addressAgentsWithInvalidCaches = this.m_agents.getAddressOfAllAgents();
                    this.m_distributionControl.clearFileCaches(addressAgentsWithInvalidCaches);
                }

                Result var4;
                try {
                    final int index = this.m_fileIndex;
                    final File file = this.m_files[index];
//                    Address addressAgentsWithoutFile = this.m_agents.getAddressOfOutOfDateAgents((new File(this.m_directory, file.getPath())).lastModified());
                    Address addressAgentsWithoutFile = this.m_agents.getAddressOfAllAgents();
                    this.m_distributionControl.sendFile(addressAgentsWithoutFile, new FileContents(this.m_directory, file));
                    var4 = new Result() {
                        public int getProgressInCents() {
                            return (index + 1) * 100 / FileDistributionHandlerImplementation.this.m_files.length;
                        }

                        public String getFileName() {
                            return file.getPath();
                        }
                    };
                } finally {
                    ++this.m_fileIndex;
                }

                return var4;
            } else {
                this.m_distributionControl.setHighWaterMark(this.m_agents.getAddressOfAllAgents(), this.m_cacheParameters.createHighWaterMark(this.m_latestFileTime));
                return null;
            }
        } catch (OutOfDateException var9) {
            return null;
        }
    }
}
