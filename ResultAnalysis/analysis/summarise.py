from glob import glob

import pandas as pd

from analysis import save_dataframe, categories, parse_args


def main(args):
    """
     Summarises analysis results by calculating the means,
     the standard deviations and the medians.
    """
    if args.split_paradigm_score:
        summarise_split_directory(args, 'univariate')
    else:
        summarise_directory(args, 'univariate')
    if args.multivariate_baseline:
        summarise_directory(args, 'multivariate-baseline')
        summarise_directory(args, 'multivariate-baseline-hasdata')
    if args.multivariate_baseline_control:
        summarise_directory(args, 'mulitvariate-baseline-control')


def summarise_directory(args, directory, columns=None):
    if columns is None:
        columns = ['name', 'precision', 'recall', 'mcc']
    folder = f'{args.folder}/regression/{directory}/'
    for category in categories:
        path = folder + category
        df = read_all(path, columns)
        if df is not None:
            summarise(df, path, category, directory)


def summarise_split_directory(args, directory, columns=None):
    if columns is None:
        columns = ['name', 'precision', 'recall', 'mcc']
    folder = f'{args.folder}/split-regression/{directory}/'
    for category in categories:
        path = folder + category
        for paradigm in ['Neutral', 'OOP', 'FP', 'Mix']:
            df = read_all(path, columns, paradigm)
            if df is not None:
                summarise(df, path, category + paradigm, directory, paradigm)


def summarise(df, path, category, directory, suffix=''):
    print(f'Summarise {category} {directory}')
    counts = df.groupby('name').size().to_frame('count')
    means = df.groupby('name').agg(['mean', 'std'])
    means.columns = means.columns.map(' '.join)
    means = counts.join(means)
    save_dataframe(means, path, 'means' + suffix)
    medians = df.groupby('name').median()
    medians = counts.join(medians)
    save_dataframe(medians, path, 'medians' + suffix)


def read_all(path, columns, paradigm=None):
    if paradigm:
        files = glob('../data/analysisResults/' + path + '/[!m]*' + paradigm + '.csv')
    else:
        files = glob('../data/analysisResults/' + path + '/[!m]*.csv')
    if files:
        all_df = pd.DataFrame(columns=columns)
        for file in files:
            df = pd.read_csv(file)[columns]
            all_df = all_df.append(df, ignore_index=True)
        return all_df
    else:
        return None


if __name__ == '__main__':
    main(parse_args())
