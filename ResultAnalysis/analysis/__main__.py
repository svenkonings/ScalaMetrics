from analysis import descriptive, fault_statistics, univariate, multivariate, parse_args, multivariate_baseline, \
    multivariate_baseline_hasdata, multivariate_baseline_control, fault_metric_statistics

if __name__ == '__main__':
    args = parse_args()
    descriptive.main(args)
    fault_statistics.main(args)
    fault_metric_statistics.main(args)
    univariate.main(args)
    multivariate.main(args)
    if args.multivariate_baseline:
        multivariate_baseline.main(args)
        multivariate_baseline_hasdata.main(args)
    if args.multivariate_baseline_control:
        multivariate_baseline_control.main(args)
