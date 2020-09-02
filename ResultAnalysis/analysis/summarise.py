import argparse
from glob import glob

import pandas as pd

from analysis import save_dataframe, categories


def main(args):
    folder = f'{args.folder}/regression/univariate/'
    for category in categories:
        path = folder + category
        df = read_all(path, ['name', 'precision', 'recall', 'mcc'])
        if df is not None:
            print(f'Summarise {category}')
            means = df.groupby('name').agg(['mean', 'std'])
            means.columns = means.columns.map(' '.join)
            save_dataframe(means, path, 'means')
            medians = df.groupby('name').median()
            save_dataframe(medians, path, 'medians')


def read_all(path, columns):
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
    parser = argparse.ArgumentParser()
    parser.add_argument('--folder', help='Select folder to analyse', dest='folder', required=True)
    main(parser.parse_args())
