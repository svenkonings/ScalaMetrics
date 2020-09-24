from analysis import descriptive, fault_statistics, univariate, multivariate, summarise, parse_args, \
    multivariate_baseline

if __name__ == '__main__':
    args = parse_args()
    descriptive.main(args)
    fault_statistics.main(args)
    univariate.main(args)
    multivariate.main(args)
    if args.multivariate_baseline:
        multivariate_baseline.main(args)
    summarise.main(args)
