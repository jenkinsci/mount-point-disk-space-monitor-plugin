/*
 * The MIT License
 *
 * Copyright 2013 Jesse Glick.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.mountPointDiskSpaceMonitor;

import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.node_monitors.AbstractDiskSpaceMonitor;
import hudson.node_monitors.DiskSpaceMonitorDescriptor;
import java.io.IOException;
import java.text.ParseException;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.ObjectUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class MountPointDiskSpaceMonitor extends AbstractDiskSpaceMonitor {

    public MountPointDiskSpaceMonitor() {}

    @DataBoundConstructor public MountPointDiskSpaceMonitor(String freeSpaceThreshold) throws ParseException {
        super(freeSpaceThreshold);
	}

    @Override public String getColumnCaption() {
        if (!Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER)) {
            return null;
        }
        String mountPoint = Jenkins.getInstance().getDescriptorByType(DescriptorImpl.class).mountPoint;
        if (mountPoint == null) {
            return null;
        }
        return "Free Disk Space at " + mountPoint;
    }

    @Extension public static class DescriptorImpl extends DiskSpaceMonitorDescriptor {

        public DescriptorImpl() {
            load();
        }

        private String mountPoint;

        @Override public String getDisplayName() {
            return "Free Disk Space at Mount Point";
        }

        @Override public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            String _mountPoint = Util.fixEmptyAndTrim(json.optString("mountPoint"));
            if (!ObjectUtils.equals(mountPoint, _mountPoint)) {
                mountPoint = _mountPoint;
                save();
            }
            return true;
        }

        public String getMountPoint() {
            return mountPoint;
        }

        @Override protected DiskSpace monitor(Computer c) throws IOException, InterruptedException {
            Node n = c.getNode();
            if (n == null) {
                return null;
            }
            if (mountPoint == null) {
                return null;
            }
            FilePath p = n.createPath(mountPoint);
            if (p == null) {
                return null;
            }
            return p.act(new GetUsableSpace());
        }

    }

}
