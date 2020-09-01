import argparse

from analysis import categories, projects, save_dataframe, get_metric_results, get_columns


def main(args):
    folder = f'{args.folder}/regression/descriptive/'
    for category in categories:
        for path, name in projects.items():
            print(f'[{category}] Descriptive: {name}')
            df = get_metric_results(args.folder, path, category)
            columns = get_columns(df)
            statistics = df[columns].describe().T.rename_axis('name', axis=0)
            statistics['count'] = statistics['count'].astype(int)
            save_dataframe(statistics, folder + category, name)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--folder', help='Select folder to analyse', dest='folder', required=True)
    main(parser.parse_args())
