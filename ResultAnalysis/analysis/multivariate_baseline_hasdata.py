import warnings

import pandas as pd
from sklearn.exceptions import ConvergenceWarning
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import StratifiedKFold, cross_val_predict

from analysis import categories, projects, save_dataframe, get_metric_results, get_columns, to_binary, get_stats, \
    parse_args
from analysis.summarise import summarise_directory


def main(args):
    """
     For each of the avialable metrics, runs multivariate regression
     on the baseline metric set with one of the metrics added.
     Only rows for which the metric has data are taken into account.
    """
    warnings.filterwarnings("ignore", category=ConvergenceWarning, module="sklearn")
    folder = f'{args.folder}/regression/multivariate-baseline-hasdata/'
    estimator = LogisticRegression(class_weight='balanced', random_state=42)
    cv = StratifiedKFold(n_splits=10, shuffle=True, random_state=42)
    for category in categories:
        for path, name in projects.items():
            baseline_df = get_metric_results('baseline', path, category)
            df = get_metric_results(args.folder, path, category)
            if df is None and 'object' in category:
                base_category = 'objectResultsBriand' if 'Briand' in category else 'objectResultsLandkroon'
                df = get_metric_results(args.folder, path, base_category)
            if baseline_df is not None and df is not None:
                multivariate_baseline_hasdata(baseline_df, df, folder, category, name, estimator, cv, args)
    summarise_directory(args, 'multivariate-baseline-hasdata')


def multivariate_baseline_hasdata(baseline_df, df, folder, category, name, estimator, cv, args):
    print(f'[{category}] Mulitvariate baseline hasdata: {name}')
    faults = df['faults'].apply(to_binary)
    result = pd.DataFrame(
        columns=['name', 'tn', 'fp', 'fn', 'tp', 'r2', 'precision', 'recall', 'mcc']
    )
    baseline_columns = get_columns(baseline_df, args)
    baseline_data = baseline_df[baseline_columns]
    columns = get_columns(df, args)
    for column in columns:
        print(f'[{category}] {name}: {column}')
        rows = df[column] != 0
        column_faults = faults[rows]
        if len(column_faults[column_faults == 0]) < 10:
            print(f'Less than 10 non-faulty column results -- skipping {column}!')
            continue
        if len(column_faults[column_faults == 1]) < 10:
            print(f'Less than 10 faulty column results -- skipping {column}!')
            continue
        column_baseline_data = baseline_data[rows]
        prediction = cross_val_predict(estimator, column_baseline_data, column_faults, cv=cv)
        baseline_result = get_stats(column_faults, prediction)
        baseline_result['name'] = f'{column} baseline'
        result = result.append(baseline_result, ignore_index=True)
        column_data = df[column][rows]
        data = column_baseline_data.join(column_data)
        prediction = cross_val_predict(estimator, data, column_faults, cv=cv)
        column_result = get_stats(column_faults, prediction)
        column_result['name'] = column
        result = result.append(column_result, ignore_index=True)

    if len(result) > 0:
        save_dataframe(result, folder + category, name, False)


if __name__ == '__main__':
    main(parse_args())
