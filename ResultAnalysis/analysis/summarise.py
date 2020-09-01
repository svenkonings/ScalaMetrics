import argparse
from glob import glob

import pandas as pd

from analysis import save_dataframe, categories


def main(args):
    folder = f'{args.folder}/regression/univariate/'
    for category in categories:
        print(f'Summarise {category}')
        summarise_univariate(folder + category)


def summarise_univariate(path):
    df = read_all(path, ['name', 'precision', 'recall', 'mcc'])
    means = df.groupby('name').agg(['mean', 'std'])
    means.columns = means.columns.map(' '.join)
    save_dataframe(means, path, 'means')
    medians = df.groupby('name').median()
    save_dataframe(medians, path, 'medians')


def read_all(path, columns):
    all_df = pd.DataFrame(columns=columns)
    for file in glob('../data/analysisResults/' + path + '/[!m]*.csv'):
        df = pd.read_csv(file)[columns]
        all_df = all_df.append(df, ignore_index=True)
    return all_df


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--folder', help='Select folder to analyse', dest='folder', required=True)
    main(parser.parse_args())
