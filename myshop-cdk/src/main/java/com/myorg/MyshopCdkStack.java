package com.myorg;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.cloudfront.*;
import software.amazon.awscdk.services.s3.*;
import software.amazon.awscdk.services.s3.deployment.BucketDeployment;
import software.amazon.awscdk.services.s3.deployment.Source;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class MyshopCdkStack extends Stack {
    public MyshopCdkStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public MyshopCdkStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // Create S3 Bucket
        Bucket websiteBucket = Bucket.Builder.create(this, "MyShopBucket")
            .websiteIndexDocument("index.html") // Enable static website hosting
            .websiteErrorDocument("index.html")
            .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
            .encryption(BucketEncryption.S3_MANAGED)
            .removalPolicy(RemovalPolicy.DESTROY)
            .autoDeleteObjects(true)
            .build();


        // Create Origin Access Control
        CfnOriginAccessControl oac = CfnOriginAccessControl.Builder.create(this, "MyShopOAC")
            .originAccessControlConfig(CfnOriginAccessControl.OriginAccessControlConfigProperty.builder()
                .name("MyShopOAC")
                .originAccessControlOriginType("s3")
                .signingBehavior("always")
                .signingProtocol("sigv4")
                .build())
            .build();

        // Create CloudFront Distribution
        CfnDistribution distribution = CfnDistribution.Builder.create(this, "MyshopDistribution")
            .distributionConfig(CfnDistribution.DistributionConfigProperty.builder()
                .enabled(true)
                .defaultRootObject("index.html")
                .origins(List.of(CfnDistribution.OriginProperty.builder()
                    .id("S3Origin")
                    .domainName(websiteBucket.getBucketRegionalDomainName())
                    .originAccessControlId(oac.getAttrId())
                    .s3OriginConfig(CfnDistribution.S3OriginConfigProperty.builder()
                        .build())
                    .build()))
                .defaultCacheBehavior(CfnDistribution.DefaultCacheBehaviorProperty.builder()
                    .targetOriginId("S3Origin")
                    .viewerProtocolPolicy("redirect-to-https")
                    .allowedMethods(List.of("GET", "HEAD"))
                    .cachedMethods(List.of("GET", "HEAD"))
                    .cachePolicyId("658327ea-f89d-4fab-a63d-7e88639e58f6") // CachingOptimized policy ID
                    .build())
                .customErrorResponses(List.of(
                    CfnDistribution.CustomErrorResponseProperty.builder()
                        .errorCode(403)
                        .responseCode(200)
                        .responsePagePath("/index.html")
                        .build(),
                    CfnDistribution.CustomErrorResponseProperty.builder()
                        .errorCode(404)
                        .responseCode(200)
                        .responsePagePath("/index.html")
                        .build()))
                .build())
            .build();

        // Add S3 bucket policy
        PolicyStatement policyStatement = PolicyStatement.Builder.create()
            .effect(Effect.ALLOW)
            .actions(List.of("s3:GetObject"))
            .resources(List.of(websiteBucket.arnForObjects("*")))
            .principals(List.of(new ServicePrincipal("cloudfront.amazonaws.com")))
            .conditions(Map.of(
                "StringEquals", Map.of(
                    "AWS:SourceArn", String.format("arn:aws:cloudfront::%s:distribution/%s",
                        Stack.of(this).getAccount(),
                        distribution.getAttrId())
                )
            ))
            .build();

        websiteBucket.addToResourcePolicy(policyStatement);

        // Deploy React App to S3
        BucketDeployment.Builder.create(this, "DeployReactApp")
            .sources(List.of(Source.asset("../dist")))
            .destinationBucket(websiteBucket)
            .distribution(Distribution.fromDistributionAttributes(this, "ImportedDist",
                DistributionAttributes.builder()
                    .distributionId(distribution.getAttrId())
                    .domainName(distribution.getAttrDomainName())
                    .build()))
            .distributionPaths(List.of("/*"))
            .build();

        // Stack Outputs
        new CfnOutput(this, "BucketName", CfnOutputProps.builder()
            .value(websiteBucket.getBucketName())
            .description("S3 Bucket Name")
            .build());

        new CfnOutput(this, "DistributionId", CfnOutputProps.builder()
            .value(distribution.getAttrId())
            .description("CloudFront Distribution ID")
            .build());

        new CfnOutput(this, "DistributionDomain", CfnOutputProps.builder()
            .value(distribution.getAttrDomainName())
            .description("CloudFront Distribution Domain Name")
            .build());
    }
}
