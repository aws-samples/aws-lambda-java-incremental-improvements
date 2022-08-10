# AWS Lambda Java incremental improvements

This repository contains AWS Lambda functions with incremental improvements for Java.
The improvement ranging from high level no-code changes to experimental changes that will very likely increase the
performance of your workload.

All the deployed functions in this sample performs the same task: Store a "product" in a DynamoDB Table and in an S3
bucket

## Description of functions and improvements

*Requires no code changes*

0. Baseline
1. Memory optimized - uses 2GB
2. Package as UberJar
3. Package as a zipped UberJar
4. Use Tiered Compilation optimization

*Requires code changes*

5. Use eager initialization of handler dependencies
6. Apply AWS SDK optimizations (credentials/region resoltion, Http Client etc)
7. Apply multi threading
8. AWS Kotlin SDK
9. CrtHttpClient

*Advanced*

10. AWS SDK warmup call
11. GraalVM

## Getting started

Download or clone the repository.

Install [AWS CDK
](https://docs.aws.amazon.com/cdk/v2/guide/getting_started.html)

Deploy:

```bash
./deploy.sh
```

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This library is licensed under the MIT-0 License. See the LICENSE file.

