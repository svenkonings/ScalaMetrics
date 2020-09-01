import argparse

from analysis import descriptive, fault_statistics, univariate, multivariate, summarise

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--folder', help='Select folder to analyse', dest='folder', required=True)
    args = parser.parse_args()
    descriptive.main(args)
    fault_statistics.main(args)
    univariate.main(args)
    multivariate.main(args)
    summarise.main(args)
