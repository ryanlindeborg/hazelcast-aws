/*
 * Copyright 2020 Hazelcast Inc.
 *
 * Licensed under the Hazelcast Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://hazelcast.com/hazelcast-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.hazelcast.aws;

import com.hazelcast.aws.impl.DescribeInstancesOperation;
import com.hazelcast.aws.impl.Ec2OperationClient;
import com.hazelcast.aws.utility.Environment;

import java.util.Map;

import static com.hazelcast.aws.impl.Constants.AWS_EXECUTION_ENV_VAR_NAME;
import static com.hazelcast.aws.utility.MetadataUtils.getEc2AvailabilityZone;
import static com.hazelcast.aws.utility.StringUtil.isNotEmpty;

/**
 * Strategy for discovery of Hazelcast instances running under EC2
 */
class Ec2ClientStrategy extends AwsClientStrategy {

    private static final String UPPER_EC2 = "EC2";

    public Ec2ClientStrategy(AwsConfig awsConfig, String endpoint) {
        super(awsConfig, endpoint);
    }

    public Map<String, String> getAddresses() throws Exception {
        return new Ec2OperationClient(awsConfig, endpoint)
                .execute(new DescribeInstancesOperation(
                        awsConfig.getTagKey(),
                        awsConfig.getTagValue(),
                        awsConfig.getSecurityGroupName()));
    }

    @Override
    public String getAvailabilityZone() {
        if (runningOnEc2()) {
            return getEc2AvailabilityZone(awsConfig.getConnectionTimeoutSeconds(), awsConfig.getConnectionRetries());
        }
        return UPPER_EC2;
    }

    private boolean runningOnEc2() {
        String execEnv = new Environment().getEnvVar(AWS_EXECUTION_ENV_VAR_NAME);
        return isNotEmpty(execEnv) && execEnv.contains(UPPER_EC2);
    }
}