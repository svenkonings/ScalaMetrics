from glob import glob

import pandas as pd

from main import save_dataframe


def summarise_univariate(path):
    all_df = pd.DataFrame()
    columns = ['name', 'precision', 'recall', 'mcc']
    for file in glob(path + '*.csv'):
        df = pd.read_csv(file)[columns]
        all_df = all_df.append(df, ignore_index=True)
    save_dataframe(all_df.groupby('name').mean().round(2), path, 'means')
    save_dataframe(all_df.groupby('name').median().round(2), path, 'medians')


if __name__ == '__main__':
    summarise_univariate('results/univariate/functions/')
    summarise_univariate('results/univariate/objects/')
